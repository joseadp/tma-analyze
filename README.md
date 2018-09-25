# TMA Analyze

This is a simple module to calculate the score of the metrics from Kubernetes.

To build the jar, you should run:

```sh
mvn -Pprod install
```

To deploy the pod in the cluster, you should run:

```sh
kubectl create -f tma-analyze.yaml
```
