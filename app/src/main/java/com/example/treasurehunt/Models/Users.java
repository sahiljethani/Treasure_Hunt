package com.example.treasurehunt.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Users
{
       private  String username;
       private String userid;
       private String emailid;
       private String profileImageUrl;



       public Users(String username, String userid, String emailid, String profileImageUrl) {
              this.username = username;
              this.userid = userid;
              this.emailid = emailid;
              this.profileImageUrl=profileImageUrl;
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

       public String getProfileImageUrl() { return profileImageUrl; }

       public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
