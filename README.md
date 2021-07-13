# HangSpot

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
HangSpot allows friend groups to map out each others' location and pick a central spot to meet up, providing a solution to the indecisiveness that comes with deciding where to meet friends. Friends can decide on a meet-up spot based on their favorite locations or the app's suggestions, or the app can randomly pick a spot for them. 

### App Evaluation
- **Category:** Social
- **Mobile:** Mobile is necessary for location services to allow friends to log their locations and view maps of others' locations in real time. It is also useful for navigating to the meet-up area once a spot has been decided on.
- **Story:** Friends, or anyone who needs to meet up with others, can quickly and conveniently find a central location to meet up and explore new places they may not have known of. 
- **Market:** Anyone facing the decision of finding a meeting place could utilize this app.
- **Habit:** An average user would use this app whenever they have trouble deciding where to meet others or when they are looking for location suggestions.
- **Scope:** The stripped-down version of this app would include getting the locations of friends, calculating the central location, having each user select a spot within a certain radius of the central location, and having the users engage in a rank-based voting system. Further versions could include randomized location selections, location suggestions, decision making for categories such as restaurants, or calculating the center based on commute times based on mode of transportation. Other features might include being able to change or remove the radius around the central location.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can register a new account
* User can login/logout
* User can invite friends to a group
* User can join existing group
* User can view a list of groups they are in
* User can calculate central location once all friends have joined
* User can select a location within a radius of the central location
* User can rank all selected locations
* User can view the final location

**Optional Nice-to-have Stories**

* User can set a radius around the central location or remove it
* User can select multiple locations near the central location
* User can view and select from suggested locations
* User can search for a location
* User can chat within the group
* User can export the final location into maps app to navigate there
* User can select to calculate the central location based on commute time/mode of transportation
* User can opt for a randomized location selection rather than the voting system
* User can use the voting-based decision making for restaurants

### 2. Screen Archetypes

* Login
   * User can login
* Register
   * User can register a new account
* Stream
    * User can view a list of groups they are in
    * User can join existing group
    * User can logout
* Creation
    * User can invite friends to a group
* Maps
    * User can calculate central location once all friends have joined
    * User can select a location within a radius of the central location
* Detail
    * User can rank all selected locations
    * User can view the final location

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Creation
* Maps (after selecting from stream)
* Detail (after selecting from stream)

**Flow Navigation** (Screen to Screen)

* Login
   * Stream
* Register
   * Stream
* Stream
    * Detail
* Creation
    * Detail
* Maps
    * Stream
    * Detail
* Detail
    * Stream
    * Maps

## Wireframes
<img src="https://i.imgur.com/OUldRAc.jpg" width=600>
<img src="https://i.imgur.com/YZp7PGG.jpg" width=600>

## Schema 

### Models
**Model: User**

| Property  | Type                                 | Description                                    |
| --------- | ------------------------------------ | ---------------------------------------------- |
| objectId  | String                               | Unique id for the user (default field)         |
| username  | String                               | Username used to log in                        |
| password  | String                               | Password used to log in                        |
| email     | String                               | Email associated with account                  |
| groups    | Array of Pointers to Group objectIds | List of groups the user is in                  |
| createdAt | DateTime                             | Date when post is created (default field)      |
| updatedAt | DateTime                             | Date when post is last updated (default field) |

**Model: Group**
| Property           | Type                           | Description                                                                                          |
| ------------------ | ------------------------------ | ---------------------------------------------------------------------------------------------------- |
| objectId           | String                         | Unique id for the user (default field)                                                               |
| users              | Array of Pointers to Users     | List of users included in the group                                                                  |
| locationCandidates | Array of Pointers to Locations | List of possible locations chosen by users in the group                                              |
| rankings           | JSON Object/map                | List of the rankings of each user                                                                    |
| status             | Number                         | Status of group - 0 for entering locations, 1 for selecting locations, 2 for voting, 3 for completed |
| userStatuses       | JSON Object/map                | List of if user has completed the task for the current status                                        |
| centralLocation    | Pointer to Location            | Calculated central location of the group                                                             |
| finalLocation      | Pointer to Location            | Final location selected by the group                                                                 |
| createdAt          | DateTime                       | Date when post is created (default field)                                                            |
| updatedAt          | DateTime                       | Date when post is last updated (default field)                                                       |

**Model: Location**

| Property    | Type             | Description                                    |
| ----------- | ---------------- | ---------------------------------------------- |
| objectId    | String           | Unique id for the user (default field)         |
| lat         | Number           | Latitude of the location                       |
| lon         | Number           | Longitude of the location                      |
| name        | String           | Name of the location                           |
| description | String           | User entered description                       |
| address     | String           | Address of the location                        |
| city        | String           | City of the location                           |
| state       | String           | State of the location                          |
| postalCode  | String           | Zip code of the location                       |
| country     | String           | Country of the location                        |
| group       | Pointer to Group | Group the location is relevant to              |
| addedBy     | Pointer to User  | User who added the location to the candidates  |
| createdAt   | DateTime         | Date when post is created (default field)      |
| updatedAt   | DateTime         | Date when post is last updated (default field) |

  
### Networking
* Log In Screen
    * (Read/GET) Log the User in
* Sign Up Screen
    * (Create/POST) Create a new User
* Groups Screen
    * (Read/GET) Query all groups the User is in
* Compose Screen
    * (Create/POST) Create a new Group
    * (Read/GET) Query existing users
* Selecting Location Detail Screen
    * (Read/GET) Query location candidates
    * (Update/PUT) Update userStatuses when a user is done
    * (Update/PUT) Update status of the Group when everyone is done
    * (Read/GET) Query for status of group to know when to move to the next screen
* Map Screen
    * (Read/GET) Query location candidates
    * (Read/GET) Query central location
    * (Create/POST) Create a new location
    * (Update/PUT) Add a new location to the locationCandidates
* Voting Detail Screen
    * (Create/POST) Create a new ranking
    * (Read/GET) Query for all location candidates
    * (Update/PUT) Update the rankings in the group
    * (Update/PUT) Update the userStatuses when user completes ranking
    * (Update/PUT) Update the group status when everyone has voted
    * (Read/GET) Query for status of group to know when to move to the next screen
* Final Location Detail Screen
    * (Read/GET) Query for final location of group

Create/POST (New ranking)
```
ParseObject message = ParseObject.create("Ranking");
message.put(USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());
message.put(LOCATION_RANKS_KEY, data);
message.put(GROUP_ID_KEY, group);
message.saveInBackground(new SaveCallback() {
    @Override
    public void done(ParseException e) {
        if (e == null) {
            Log.i(TAG, "Successfully created Group");
        } else {
            Log.e(TAG, "Failed to save message", e);
        }
    }
});
```

Read/GET (All groups the user is in)
```
ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
query.include(Constants.GROUPS_KEY);
query.addDescendingOrder("updatedAt");
query.findInBackground((groups, e) -> {
   if (e != null) {
       Log.e(TAG, "Issue with loading groups", e);
       return;
   }
   allGroups.clear();
   allGroups.addAll(groups);
});
```

Update/PUT (Status of Group)
```
ParseQuery<Group> query = ParseQuery.getQuery("Group");
query.getInBackground("status", (object, e) -> {
    if (e == null) {
      object.put("status", 2);
      object.saveInBackground();
    } else {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }  
  });
```
