package model;


public class Player {

 private String username;
 private String password;
 private String id;    // optional unique identifier

 // --------- Constructors ---------

 public Player(String username, String password) {
     this(username, password, null);
 }

 public Player(String username, String password, String id) {
     this.username = username;
     this.password = password;
     this.id = id;
 }

 // --------- Getters & Setters ---------

 public String getUsername() {
     return username;
 }

 public void setUsername(String username) {
     this.username = username;
 }

 public String getPassword() {
     return password;
 }

 public void setPassword(String password) {
     this.password = password;
 }

 public String getId() {
     return id;
 }

 public void setId(String id) {
     this.id = id;
 }

 // --------- Utility ---------

 @Override
 public String toString() {
     return "Player{" +
             "username='" + username + '\'' +
             ", id='" + id + '\'' +
             '}';
     // Note: password intentionally not printed for security
 }
}

