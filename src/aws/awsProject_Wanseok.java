package aws;

import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.MonitorInstancesRequest;
import com.amazonaws.services.ec2.model.UnmonitorInstancesRequest;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;

public class awsProject_Wanseok {
    static AmazonEC2 ec2;

    private static void init() throws Exception {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        }
        catch (Exception e) {
            throw new AmazonClientException(
                "Cannot load the credentials from the credential profiles file. " +
                "Please make sure that your credentials file is at the correct " +
                "location (~/.aws/credentials), and is in valid format.",
                e);
        }
   
ec2 = AmazonEC2ClientBuilder.standard()
.withCredentials(credentialsProvider)
.withRegion("us-east-1") /* check the region at AWS console */
.build();

}
    //1. listInstances
    public static void listInstances() {
        System.out.println("Listing instances....");
        boolean done = false;
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);
            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " + "[AMI] %s, " + "[type] %s, " + "[state] %10s, " + "[monitoring state] %s",
                            instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
                            instance.getState().getName(), instance.getMonitoring().getState());
                }
                System.out.println();
            }
            request.setNextToken(response.getNextToken());
            if (response.getNextToken() == null) {
                done = true;
            }
        }
    }
    
    //2. AvailableZones
    public static void AvailableZones()
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeAvailabilityZonesResult zones_response = ec2.describeAvailabilityZones();

        for(AvailabilityZone zone : zones_response.getAvailabilityZones()) {
         	System.out.println(" ");
           	System.out.printf("Found availability zone=%s, ", zone.getZoneName());
           	System.out.printf("with status=%s, ", zone.getState());
           	System.out.printf("in region=%s \n", zone.getRegionName());
        }
    }
        
    //3. startInstance
    public static void startInstance(String instance_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
       
        DryRunSupportedRequest<StartInstancesRequest> dry_request =
            () -> {
            	StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);
                return request.getDryRunRequest();
             };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to start instance=%s", instance_id);
            throw dry_response.getDryRunResponse();
        }

        StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);
        ec2.startInstances(request);
        System.out.printf("Successfully started instance=%s", instance_id);
    }
       
    //4. AvailableRegions
    public static void AvailableRegions()
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeRegionsResult regions_response = ec2.describeRegions();

        for(Region region : regions_response.getRegions()) {
           	System.out.println(" ");
           	System.out.printf("Found region=%s, ", region.getRegionName());
           	System.out.printf("with endpoint=%s \n", region.getEndpoint());
        }
    }
        
    //5. stopInstance
    public static void stopInstance(String instance_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
            () -> {
            StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
            return request.getDryRunRequest();
            };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to stop instance=%s", instance_id);
            throw dry_response.getDryRunResponse();
        }

        StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
        ec2.stopInstances(request);
        System.out.printf("Successfully stop instance=%s", instance_id);
        }
        
    //6. CreatInstance
    public static void CreatInstance(String ami_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
            .withImageId(ami_id)
            .withInstanceType(InstanceType.T2Micro)
            .withMaxCount(1)
            .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);
        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
            "Successfully started EC2 instance=%s based on AMI=%s",reservation_id, ami_id);
    }
    
    //7. RebootInstance
    public static void RebootInstance(String instance_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instance_id);
        RebootInstancesResult response = ec2.rebootInstances(request);

        System.out.printf("Successfully rebooted instance=%s", instance_id);
    }
    //8. listImage
    public static void ListImage() {
        System.out.println("Listing Images....");
        
        DescribeImagesRequest request = new DescribeImagesRequest().withOwners("self");
        DescribeImagesResult response = ec2.describeImages(request);
        for (Image image : response.getImages()) {
            System.out.printf(
                   "[id] %s, " + "[Type] %s, " + "[Location] %s, " + "[Owner] %s",
                    image.getImageId(), image.getImageType(), image.getImageLocation(), image.getImageOwnerAlias());
        }
            System.out.println();
    }   
    //9. monitorInstance
    public static void monitorInstance(String instance_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DryRunSupportedRequest<MonitorInstancesRequest> dry_request =
            () -> {
            MonitorInstancesRequest request = new MonitorInstancesRequest()
                .withInstanceIds(instance_id);
            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if (!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to enable monitoring on instance %s",
                instance_id);

            throw dry_response.getDryRunResponse();
        }

        MonitorInstancesRequest request = new MonitorInstancesRequest().withInstanceIds(instance_id);
        ec2.monitorInstances(request);

        System.out.printf("Successfully enabled monitoring for instance=%s",instance_id);
    }
    //10. unmonitorInstance
    public static void unmonitorInstance(String instance_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DryRunSupportedRequest<UnmonitorInstancesRequest> dry_request =
            () -> {
            UnmonitorInstancesRequest request = new UnmonitorInstancesRequest().withInstanceIds(instance_id);
            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if (!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to disable monitoring on instance=%s",instance_id);

            throw dry_response.getDryRunResponse();
        }

        UnmonitorInstancesRequest request = new UnmonitorInstancesRequest().withInstanceIds(instance_id);

        ec2.unmonitorInstances(request);

        System.out.printf(
            "Successfully disabled monitoring for instance=%s",instance_id);
    }
    
public static void main(String[] args) throws Exception {
        init();
        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        
        int number = 0;
        String start_id = "";
        String stop_id = "";
        String ami_id = "";
        String reboot_id = "";
        String monitor_id = "";
        String unmonitor_id = "";
        while(true)
        {
            System.out.println(" ");
            System.out.println(" ");
            System.out.println("------------------------------------------------------------");
            System.out.println(" Amazon AWS Control Panel using SDK ");
            System.out.println(" ");
            System.out.println(" Cloud Computing, Computer Science Department ");
            System.out.println(" at Chungbuk National University ");
            System.out.println("------------------------------------------------------------");
            System.out.println(" 1. list instance   2. available zones ");
            System.out.println(" 3. start instance  4. available regions ");
            System.out.println(" 5. stop instance   6. create instance ");
            System.out.println(" 7. reboot instance 8. list images ");
            System.out.println(" 9. monitorInstance 10. unmonitorInstance ");
            System.out.println(" 99. quit ");
            System.out.println("------------------------------------------------------------");
            System.out.print("Enter an integer: ");
       
            number = menu.nextInt();
           
            switch(number) {
            case 1:
                listInstances();
                break;
            case 2:
            	AvailableZones();
                break;
            case 3:
            	System.out.printf("start instance id? : ");
                start_id = id_string.nextLine();
            	startInstance(start_id);
            	break;
            case 4:
            	AvailableRegions();
            	break;
            case 5:
            	System.out.printf("stop instance id? : ");
            	stop_id = id_string.nextLine();
            	stopInstance(stop_id);
            	break;
            case 6:
            	System.out.printf("AMI id? : ");
            	ami_id = id_string.nextLine();
            	CreatInstance(ami_id);
            	break;
            case 7:
            	System.out.printf("reboot instance id? : ");
            	reboot_id = id_string.nextLine();
            	RebootInstance(reboot_id);
            	break;
            case 8:
            	ListImage();
            	break;
            case 9:
            	System.out.printf("monitor instance id? : ");
            	monitor_id = id_string.nextLine();
            	monitorInstance(monitor_id);
            	break;
            case 10:
            	System.out.printf("unmonitor instance id? : ");
            	unmonitor_id = id_string.nextLine();
            	unmonitorInstance(unmonitor_id);
            	break;
            case 99:
            	System.exit(0);
            	break;
            }
           
           
       
        }
    }
}