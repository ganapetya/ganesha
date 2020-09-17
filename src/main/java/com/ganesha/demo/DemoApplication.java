package com.ganesha.demo;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static final String COMMAND_DEMO_POD_PREFIX = "command-demo-";
    public static final String COMMAND_DEMO_CONTAINER_PREFIX = "command-demo-container-";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    static Set<String> exitStatuses = new HashSet<String>();

    static {
        exitStatuses.add("Succeeded");
        exitStatuses.add("Failed");
        exitStatuses.add("Unknown");
    }

    private static String NAMESPACE = "default";
    private static long LOOP_TIME = 1000l;

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    private ExecutorService service = Executors.newSingleThreadExecutor();

    @PostMapping("/user")
    public String sendUser(@Valid @RequestBody UserResource userResource) {
        return String.format("Hello %s!", userResource.getName());
    }

    @GetMapping("/getPods")
    public String getPods() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        StringBuilder sb = new StringBuilder();
        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listNamespacedPod(NAMESPACE, null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            sb.append(item.getMetadata().getName());
        }
        return String.format("Hello %s!", sb.toString());
    }

    @GetMapping("/createPodGetLog")
    public String createPod() throws IOException, ApiException {

        String podId = UUID.randomUUID().toString();  //This suffix provides unique pod ID per request

        ApiClient client = Config.defaultClient();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();             //this is our interface to K8S API

        V1Pod pod = defineExampleCommandPod(podId);  //The pod defined programmatically

        V1Pod pod1 = createPod(api, pod);            //The pod created and executed

        String status = waitCompletedStatus(pod1, api);  //looping till Failure/Success

        String log = getLogs(api, pod1, podId);     //getting log

        deletePod(api, pod1);                       //deleting the pod

        return String.format("Pod (Pet hello!) exit status %s \nlogs:\n%s" ,status, log );
    }


    private V1Pod createPod(CoreV1Api api, V1Pod pod) throws ApiException {
        return api.createNamespacedPod(NAMESPACE, pod, null, null, null);
    }

    private void deletePod(CoreV1Api api, V1Pod pod1) throws ApiException{
        try {
            api.deleteNamespacedPod(pod1.getMetadata().getName(), NAMESPACE, null, null, null, null, null, null);
        }catch(Throwable e){ /*e.printStackTrace();*/System.out.println("Not a graceful delete - review it"); }//TODO this delete should be reviewed, it throws an error
    }

    private String getLogs(CoreV1Api api, V1Pod pod1, String podId) throws ApiException {
        String log =  api.readNamespacedPodLog(pod1.getMetadata().getName(), NAMESPACE, "command-demo-container-" + podId, false, true, null, null, null, null, null, null);
        System.out.println("log = " + log);
        return log;
    }

    private Callable<String> defineStatusWaitingCallable(V1Pod pod1, CoreV1Api api) {
        Callable<String> checkStatus = () -> {
            synchronized (service) {
                service.wait(LOOP_TIME);
                V1Pod pod = api.readNamespacedPodStatus(pod1.getMetadata().getName(), NAMESPACE, null);
                return pod.getStatus().getPhase();
            }
        };
        return checkStatus;
    }

    private V1Pod defineExampleCommandPod(String podId) {

        return new V1PodBuilder()
                .withNewMetadata()
                .withName(COMMAND_DEMO_POD_PREFIX + podId)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(COMMAND_DEMO_CONTAINER_PREFIX + podId)
                .withImage("debian")
                .addNewCommand("printenv")
                .addNewArg("HOSTNAME")
                .addNewArg("KUBERNETES_PORT")
                .addNewArg("HOME")
                .endContainer()
                .withRestartPolicy("Never")
                .endSpec().build();
    }

    private String waitCompletedStatus(V1Pod pod1, CoreV1Api api) {
        String result = null;
        while (!exitStatuses.contains(result)) {
            Callable<String> checkStatus = defineStatusWaitingCallable(pod1, api);
            Future<String> future = service.submit(checkStatus);
            try {
                result = future.get();
                System.out.println("pod " + pod1.getMetadata().getName() + " status : " + result);
                if (exitStatuses.contains(result)) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Exit status = " + result);
        return result;
    }

}
