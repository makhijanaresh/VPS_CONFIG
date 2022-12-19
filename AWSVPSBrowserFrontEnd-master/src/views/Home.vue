<template >
  <div id='add-blog' class='about'>
<p>Message:{{msg}}</p>
<form>
 <!-- <router-link to="/foo">Go to Foo</router-link><br/><br/><br/>-->
 <label>
   Hello User we are asking Username and password so that in each request we can connect to your  account
   kindly note we are not storing any of the confidential vlaue 
 </label><br/><br/>
 <label>Kindly note that you have to provide username and password when you want to create instance and in other requests password is not mandatory</label><br/><br/>
  <input type="text" id="username" placeholder="provide username"><br/>
  <input type="password" id="password" placeholder="provide password"><br/>
    <textarea id='softwares' placeholder="provide softwares to get installed"></textarea><br/>

    <button type="button" @click='createInstance'>CreateInstance</button>
    <br/><br/>
    <button type="button" @click='buttonClicked'>GetListOfInstanceWithState</button>
    <br/>Instances State:{{runningInstances}}<br/><br/>

    <label>Change Instance State</label>
    
    <input type="text" id="instanceState" placeholder="type either Start or Stop">
    <button type="button" @click='changeState'>Change Instance State</button><br/><br/>
    <ejs-dropdownlist id='dropdownlist' :dataSource='sportsData'></ejs-dropdownlist>
    </form>

    <br/>
    <br/>
    <label>Select tomcat version to install</label>
   <select v-model="tomcatVal" @change="dropdownchanged($event)">
    <option v-for="item in versions" :key="item">{{item}}</option>
    <ul>
     
  
</ul>
</select>
<input type="text" id="connectorport" placeholder="tomcat-main-port">
<input type="text" id="serverport" placeholder="tomcat-server-port">
 <button type="button"  @click='installtomcat'>Install Tomcat</button>
  </div>

  <br/><br/>
  <label>Select java to install</label>
   <select v-model="javaVal" @change="javadropdownchanged($event)">
    <option v-for="item in javaVersions" :key="item">{{item}}</option>
    <ul>
     
  
</ul>
</select>
<button type="button" @click='installJava'>Install Java</button>
<br/>
<label>Install Mysql</label>
<input type="password" placeholder="root password" id="root_password">
<input type="text" placeholder="user-name" id="mysql-user-name">
<input type="password" placeholder="user-password" id="mysql-user-password">
<input type="checkbox" id="allow-remote-login" ref="rolesSelected">
<input type="text" id="port" placeholder="port">
<button type="button" @click="installMysql">Install Mysql</button>
<br/>
<button type="button" @click="getInstallationStatus">GetInstallation Status</button>
</template>

<script>
import axios from 'axios';


export default {
  
 mounted() {
      this.getVersions()
    },
  data() {
    return{
      
        tomcatVal: null,
      active:true,
  msg:null,
  runningInstances:null ,
  versionInfo:null,
  versionMap:{},
  selected: '',
  versions:[],
  checkboxSelected:'',

  javaVersionMap:{},
  javaVersions:[],
  javaSelected:'',
  installationStatus:''
    }
  },
  methods:{

getInstallationStatus()
{
 // this.installationStatus='';
 axios.get("http://localhost:8081/installationStatus").
        then((response)=>{console.log(response);
        this.installationStatus=response.data});
       // if(this.installationStatus!='')
       //{
        alert(this.installationStatus);
        //}
},
    installMysql()
    {
      if(this.$refs.rolesSelected.checked == true) {
   this.checkboxSelected=true;
  }
  else{
    this.checkboxSelected=false;
  }
const info={
  "rootPassword":document.getElementById("root_password").value,
  "additionalUser":document.getElementById("mysql-user-name").value,
  "additionalUserPassword":document.getElementById("mysql-user-password").value,
  "allowRemoteLogin":this.checkboxSelected,
  "userName":document.getElementById("username").value,
  "password":document.getElementById("password").value,
  "port":document.getElementById("port").value
  

}

 axios.post("http://localhost:8081/mysql",info).
        then((response)=>{console.log(response);
        this.msg=response.data});

    },

    dropdownchanged(changedvalue)
    {
     
      this.selected=changedvalue.target.value;
    },

    javadropdownchanged(changedvalue)
    {
      //  alert(changedvalue.target.value);
      this.javaSelected=changedvalue.target.value;
     // alert(this.javaVersionMap[this.javaSelected]);
    },

   getVersions()
   {
axios.get(`http://localhost:8082/versions`)
        .then((response) => {console.log(response);
        this.versionInfo=response.data;   
        this.versionInfo.tomcat.forEach(element => {
        this.versions.push(element.version);
        this.versionMap[element.version]=element.url;
        }
        
        );
         this.versionInfo.java.forEach(element => {
        this.javaVersions.push(element.javaVersion);
        this.javaVersionMap[element.javaVersion]=element.javaCommand;
        }
         );
        }).catch((error) =>{console.log(error)});
   },

    buttonClicked()
    {
       
       let   username=document.getElementById("username").value
       console.log(username);
        axios.get(`http://localhost:8081/getInstanceInfo/${username}`)
        .then((response) => {console.log(response);
        this.runningInstances=response.data;
        document.getElementById("runningInstances").value=response.data;
        }).catch((error) =>{console.log(error)});
    },
     changeState()
{
const sgd={
"userName":document.getElementById("username").value,
"instanceState":document.getElementById("instanceState").value
}

  axios.post("http://localhost:8081/changestate",sgd).
        then((response)=>{console.log(response);
        this.msg=response.data});

},

installtomcat()
{
 //alert(this.selected);
 //alert("Username:"+document.getElementById("username").value);
const info={
 "tarPath":this.versionMap[this.selected],
 "connectorPort":document.getElementById("connectorport").value,
 "serverPort":document.getElementById("serverport").value,
 "userName":document.getElementById("username").value

}
   axios.post("http://localhost:8081/tomcat",info).
        then((response)=>{console.log(response);
        this.msg=response.data});
},
    

    installJava()
    {
      const info={
  "userName":document.getElementById("username").value,
  "javaCommand":this.javaVersionMap[this.javaSelected]
      }
       axios.post("http://localhost:8081/java",info).
        then((response)=>{console.log(response);
        this.msg=response.data});
    },
    createInstance()
    {
        const sgd={
          "userName":document.getElementById("username").value,
          "password":document.getElementById("password").value,
            "softwaresToInstall":document.getElementById("softwares").value,
    "securityGroupDetail":{
       
        
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
