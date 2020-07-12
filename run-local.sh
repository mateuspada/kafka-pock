#!/bin/sh

echo "starting compose"
docker-compose -f ./kafka-single.yml up -d

echo "waiting for kafka"
while ! kafkactl get topics; do
  sleep 1
done
sleep 3
echo "kafka ready"

kafkactl create topic kafkapock --partitions 1 --replication-factor 1
kafkactl produce kafkapock --file=./resources/message.json --header x-teste:teste