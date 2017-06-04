package com.data.analyser.dto;

public class User {
    private String id;
    private String profileName;


    public User(String id, String profileName) {
        this.id = id;
        this.profileName = profileName;
    }

    public String getId() {
        return id;
    }

    public String getProfileName() {
        return profileName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        return id.equals(user.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
