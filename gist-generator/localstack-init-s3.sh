#!/bin/sh
set -e
awslocal s3api create-bucket --bucket my-bucket --region us-east-1
