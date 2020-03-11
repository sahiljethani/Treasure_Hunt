package com.example.treasurehunt.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Users
{
       public  String username;
       public String userid;
       public String emailid;


       public Users(String username, String userid, String emailid) {
              this.username = username;
              this.userid = userid;
              this.emailid = emailid;
       }

       public Users()
       {

       }


       public String getUsername() {
              return username;
       }

       public void setUsername(String username) {
              this.username = username;
       }

       public String getUserid() {
              return userid;
       }

       public void setUserid(String userid) {
              this.userid = userid;
       }

       public String getEmailid() {
              return emailid;
       }

       public void setEmailid(String emailid) {
              this.emailid = emailid;
       }
}
