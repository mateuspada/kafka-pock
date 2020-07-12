https://github.com/simplesteph/kafka-stack-docker-compose

kfk create topic kafkapock --partitions 1 --replication-factor 1
kfk produce kafkapock --file=./resources/message.json --header x-teste:teste
kfk consume kafkapock --from-beginning --print-keys --print-timestamps --print-headers -o json --verbose

https://deviceinsight.github.io/kafkactl/
https://github.com/deviceinsight/kafkactl