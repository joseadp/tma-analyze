# Case Study Deployment

In order to deploy the case study of TMA-framework, you will need the following components:

* **TMA Framework**: deploy the `TMA-Monitor` and `TMA-Knowledge` as described in the [instructions](https://github.com/eubr-atmosphere/tma-framework/wiki/F.A.Q.-on-Platform-Deployment);
* **Probe**: deploy the `probe-k8s-metrics-server` from the branch [feature/probe-k8s-improvement](https://github.com/eubr-atmosphere/tma-framework-m/tree/feature/probe-k8s-improvement); Follow the [instructions](https://github.com/eubr-atmosphere/tma-framework-m/tree/feature/probe-k8s-improvement/development/probes/probe-k8s-metrics-server). (Do not forget to deploy metric-servers as explained on the instructions.)
* **TMA-Analyze**: deploy as describe on the [instructions](https://github.com/joseadp/tma-analyze);
* **TMA-Planning**: deploy as describe on the [instructions](https://github.com/joseadp/tma-planning);
* **TMA-Execute**: deploy as describe on the [instructions](https://github.com/joseadp/tma-execute);
* **Actuator**: deploy the [actuator](https://github.com/eubr-atmosphere/tma-framework-e/tree/master/development/actuators/kubernetes-actuator). It is a java springboot application. This is the only component that is not on kubernetes, and you will need the following [library](https://github.com/eubr-atmosphere/tma-framework-e/tree/master/development/libraries/java-actuator-base).

The instructions above will let you deploy the environment. You will also need to configure the data on knowledge database. This can be done through the [tma-admin-console](https://github.com/eubr-atmosphere/tma-framework-k/tree/master/development/tma-admin-console). Admin still does not allow you adding probes, and you will need to do it manually.
