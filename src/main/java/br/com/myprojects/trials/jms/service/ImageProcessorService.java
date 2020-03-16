package br.com.myprojects.trials.jms.service;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import br.com.myprojects.trials.jms.domain.model.Documento;
import br.com.myprojects.trials.jms.domain.model.HistoricoProcessamentoImagem;
import br.com.myprojects.trials.jms.domain.model.Usuario;
import br.com.myprojects.trials.jms.repository.HistoricoProcessamentoRepository;
import br.com.myprojects.trials.jms.service.exceptions.ImagemProcessorException;
import br.com.myprojects.trials.jms.util.AwsS3Utils;
import br.com.myprojects.trials.jms.util.ImagemProcessorUtils;
import br.com.myprojects.trials.jms.util.ResourceBundleUtils;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ImagemProcessorService {

    private final ApplicationProperties applicationProperties;
    private final ResourceBundleUtils resourceBundleUtils;
    private final HistoricoProcessamentoRepository historicoProcessamentoRepository;
    private final DocumentoService documentoService;

    private AmazonS3 s3;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ImagemProcessorService(
            ApplicationProperties applicationProperties,
            ResourceBundleUtils resourceBundleUtils,
            HistoricoProcessamentoRepository historicoProcessamentoRepository,
            DocumentoService documentoService) {
        this.applicationProperties = applicationProperties;
        this.resourceBundleUtils = resourceBundleUtils;
        this.historicoProcessamentoRepository = historicoProcessamentoRepository;
        this.documentoService = documentoService;
    }

    public void process(String sourceKey) {
        String sourceBucket = applicationProperties.getAws().getS3SourceBucket();

        // checks if object key exists in origin bucket (images to proccess)
        try (S3Object s3Object = AwsS3Utils.getObject(getAwsS3Client(), sourceBucket, sourceKey)) {
            // s3 object (image) should exists
            if (s3Object != null) {
                // vars from s3 object metadata and key
                long size = s3Object.getObjectMetadata().getContentLength();
                String origin = s3Object.getObjectMetadata().getUserMetaDataOf("origin");
                String subject = s3Object.getObjectMetadata().getUserMetaDataOf("subject");
                String cnpj =  s3Object.getObjectMetadata().getUserMetaDataOf("agent");

                String chassi = ImagemProcessorUtils.extractChassiFromKey(sourceKey);
                String extension = ImagemProcessorUtils.extractFileExtensionFromKey(sourceKey);

                HistoricoProcessamentoImagem history = HistoricoProcessamentoImagem.builder()
                        .caminhoBucketTemp(sourceKey)
                        .origem(origin)
                        .usuario(subject)
                        .chassi(chassi)
                        .cnpj(cnpj)
                        .dtProcessamento(OffsetDateTime.now())
                        .build();

                // if object was found and successfully associated to a document
                boolean associated = false;

                // should find both user and chassi from an existing document
                Optional<Pair<Usuario, Documento>> pair = documentoService.findUserAndDocument(subject, chassi);
                if (pair.isPresent()) {
                    associated = processPair(pair.get(), history, sourceKey, extension, size);
                    history.setAssociado(associated);
                } else {
                    String message = String.format(resourceBundleUtils.getMessage("error.attachment.document.not.found"), sourceKey);
                    history.setObservacoes(message);
                }

                historicoProcessamentoRepository.save(history);

                if (associated || copyToProcessedFolder(sourceBucket, sourceKey)) {
                    AwsS3Utils.removeObject(getAwsS3Client(), sourceBucket, sourceKey);
                }

                // only reads object metadata then cancels data stream
                s3Object.getObjectContent().abort();
            }
        } catch (AmazonServiceException | IOException var0) {
            log.error(var0.getMessage(), var0);
            throw new AmazonS3Exception("Error while connecting to Amazon S3:", var0);
        } catch (Exception var1) {
            log.error(var1.getMessage(), var1);
            throw new ImagemProcessorException(String.format("Error processing key %s", sourceKey));
        }
    }

    private boolean processPair(
            Pair<Usuario, Documento> pair,
            HistoricoProcessamentoImagem historico,
            String sourceKey,
            String extension,
            long size) {

        Documento document = pair.getRight();
        Usuario user = pair.getLeft();
        String destinationKey = buildDestinationKey(document.getEstado().getId(), document.getId(), extension);
        if (copyToDestinationBucket(sourceKey, destinationKey)) {
            documentoService.updateDocument(document, user, destinationKey, extension, size, historico.getOrigem());
            historico.setIdDocumento(document.getId());
            historico.setCaminhoBucketFinal(destinationKey);
            return true;
        } else {
            String message = String.format(resourceBundleUtils.getMessage("error.attachment.s3.upload.failed"), sourceKey);
            historico.setObservacoes(message);
        }
        return false;
    }

    @SneakyThrows
    private String buildDestinationKey(String idEstado, long idDocumento, String extension) {
        val anoData = LocalDate.now().getYear();
        val mesData = LocalDate.now().getMonthValue();
        val diaData = LocalDate.now().getDayOfMonth();

        return applicationProperties.getFiles().getDestinationPrefix()
                .concat(idEstado)
                .concat("/")
                .concat(String.valueOf(anoData))
                .concat("/")
                .concat((mesData <= 9 ? "0".concat(String.valueOf(mesData)) : String.valueOf(mesData)))
                .concat("/")
                .concat((diaData <= 9 ? "0".concat(String.valueOf(diaData)) : String.valueOf(diaData)))
                .concat("/")
                .concat(String.format("documento_original_%06d.%s", idDocumento, extension));
    }

    @SneakyThrows
    private boolean copyToDestinationBucket(final String sourceKey, final String destinationKey) {
        return AwsS3Utils.copyObject(getAwsS3Client(),
                applicationProperties.getAws().getS3SourceBucket(),
                applicationProperties.getAws().getS3DestinationBucket(),
                sourceKey, destinationKey);
    }

    @SneakyThrows
    private boolean copyToProcessedFolder(String sourceBucket, String sourceKey) {
        // todo: arrumar
        return AwsS3Utils.copyObjectToSameBucket(getAwsS3Client(), sourceBucket, sourceKey,
                ImagemProcessorUtils.createProcessedKey(
                        sourceKey, applicationProperties.getFiles().getProcessedPrefix()));
    }

    // stores s3 client in request scope of this service
    private AmazonS3 getAwsS3Client() {
        if (s3 == null) {
            s3 = AwsS3Utils.createClient(
                    applicationProperties.getAws().getAccessKeyId(),
                    applicationProperties.getAws().getSecretAccessKey(),
                    Regions.fromName(applicationProperties.getAws().getRegion()));
        }
        return s3;
    }
}
