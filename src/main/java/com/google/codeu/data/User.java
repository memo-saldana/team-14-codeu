package com.google.codeu.data;

public class User {

  private String email;
  private String firstName;
  private String lastName;
  private String country;
  private String city;
  private String aboutMe;
  private String recommendation;

  public User(String email, String firstName, String lastName, String country, String city, String aboutMe, String recommendation) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.country = country;
    this.city = city;
    this.aboutMe = aboutMe;
    this.recommendation = recommendation;
  }

  public String getEmail(){
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName(){
    return lastName;
  }

  public String getName() {
    return firstName + " " + lastName;
  }

  public String getCountry() {
    return country;
  }

  public String getCity() {
    return city;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public String getRecommendation() {
    return recommendation;
  }
}
