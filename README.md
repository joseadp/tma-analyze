# TMA Analyze

This is a simple module to calculate the score of the metrics from Kubernetes.

To build the jar, you should run the following command on the worker node:
```sh
mvn -Pprod install
```

The analyzer will calculate the scores and add them to the topic `topic-planning`. To create the topic, you should run on the master node:
```sh
kubectl exec -ti kafka-0 -- kafka-topics.sh --create --topic topic-planning --zookeeper zk-0.zk-hs.default.svc.cluster.local:2181 --partitions 1 --replication-factor 1
```

To deploy the pod in the cluster, you should run the following command on the master node:

```sh
kubectl create -f tma-analyze.yaml
```

You can also check the items on topic. In order to do that, you should connect to the Kafka pod and execute the consumer:
```sh
kubectl exec -ti kafka-0 -- bash
kafka-console-consumer.sh --topic topic-planning --bootstrap-server localhost:9093
```
