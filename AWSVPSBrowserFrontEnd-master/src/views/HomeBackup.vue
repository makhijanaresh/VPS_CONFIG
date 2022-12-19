<template>
  <div id='add-blog' class='about'>
<p>Message:{{msg}}</p>
<form>
 <!-- <router-link to="/foo">Go to Foo</router-link><br/><br/><br/>-->
 <label>
   Hello User we are asking Access Key,Secret Key and Pem file name so that in each request we can connect to your aws account
   kindly note we are not storing any of the confidential vlaue 
 </label>
  <input type="password" id="access_key" placeholder="provide access key here"><br/>
  <input type="password" id="secret_key" placeholder="provide secret key here"><br/>
  <input type="password" id="pem_key" placeholder="provide pem file name only"><br/><br/><br/>
    <textarea id='softwares' placeholder="List of installed softwares"></textarea><br/>
<label>Please use a unique name, every time new security group will get created</label><br/>
    <input type="text" id='securityGroupName' placeholder="Provide SecurityGroupName" required>
    <button type="button" @click='createInstance'>CreateInstance</button>
    <br/><br/>
    <button type="button" @click='buttonClicked'>GetListOfInstanceWithState</button>
    <br/>Instances State:{{runningInstances}}<br/><br/>

    <label>Change Instance State</label>
    
    <input type="text" id="instanceState" placeholder="type either Start or Stop">
    <input type="text"  id="instanceId" placeholder="Type instance id">
    <button type="button" @click='changeState'>Change Instance State</button>
    </form>

    <br/>
    <br/>


  </div>

  
</template>

<script>
import axios from 'axios'

export default {
  
 mounted() {
    //  this.buttonClicked()
    },
  data() {
    return{
  msg:null,
  runningInstances:null ,
    }
  },
  methods:{

   

    buttonClicked()
    {
      //  const field = document.getElementById("softwares").value;
       // alert(field);
       const sgd={
         "accessKey":document.getElementById("access_key").value,
          "secretKey":document.getElementById("secret_key").value,
          "pemFileName":document.getElementById("pem_key").value
       }
        axios.post('http://localhost:8081/listAllInstances',sgd)
        .then((response) => {console.log(response);
        this.runningInstances=response.data;
        document.getElementById("runningInstances").value=response.data;
        }).catch((error) =>{console.log(error)});
    },
     changeState()
{
const sgd={
"instanceId":document.getElementById("instanceId").value,
"instanceState":document.getElementById("instanceState").value,
 "accessKey":document.getElementById("access_key").value,
          "secretKey":document.getElementById("secret_key").value,
          "pemFileName":document.getElementById("pem_key").value
}

  axios.post("http://localhost:8081/changestate",sgd).
        then((response)=>{console.log(response);
        this.msg=response.data});

},
    
    createInstance()
    {
        const sgd={
          "accessKey":document.getElementById("access_key").value,
          "secretKey":document.getElementById("secret_key").value,
          "pemFileName":document.getElementById("pem_key").value,
            "softwaresToInstall":document.getElementById("softwares").value,
    "securityGroupDetail":{
        "name":document.getElementById("securityGroupName").value,
        
        "description":"description of test ",
        "portDetails":[
           { "port":"22",
            "ip":"0.0.0.0/0",
            "protocol":"tcp"
           },
           { "port":"8080",
            "ip":"0.0.0.0/0",
            "protocol":"tcp"
           }
        ]
    }
    
}
       // alert("post got invoked");
        axios.post("http://localhost:8081/createInstance",sgd).
        then((response)=>{console.log(response);
        this.msg=response.data});
    }
  }
}
</script>



<style>
#app {
  margin: 0;
  font-family: 'Nunito SemiBold'
}
</style>
