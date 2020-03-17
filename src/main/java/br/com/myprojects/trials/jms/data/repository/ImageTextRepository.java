package br.com.myprojects.trials.jms.data.repository;

import br.com.myprojects.trials.jms.data.entity.ImageText;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageTextRepository extends MongoRepository<ImageText, String> {

}
