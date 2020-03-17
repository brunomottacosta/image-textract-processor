#!/usr/bin/env python
import os
import boto3

# lambda function that is invoked from an event of uploaded image in s3
def handler(event, context):
    print('\n\n')
    for record in event['Records']:

        key = record['s3']['object']['key']
        print('ObjectKey:', key)

        sqs = boto3.client('sqs',
            # access key from aws must be set in environment variables
            region_name=os.environ['REGION_NAME'],
            aws_access_key_id=os.environ['ACCESS_KEY_ID'],
            aws_secret_access_key=os.environ['SECRET_ACCESS_KEY'])
        response = sqs.send_message(
            QueueUrl=os.environ['SQS_QUEUE_URL'],
            MessageBody=key,
            MessageGroupId='ProcessImagesQ',
            MessageDeduplicationId=key
        )

        # The response is NOT a resource, but gives you a message ID and MD5
        print('MessageId:', response.get('MessageId'))
        print('MD5OfMessageBody:', response.get('MD5OfMessageBody'))

    print('\n\n')
