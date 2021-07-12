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


<table>
  <tr>
   <td><strong>Property</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>objectId
   </td>
   <td>String
   </td>
   <td>Unique id for the user (default field)
   </td>
  </tr>
  <tr>
   <td>username
   </td>
   <td>String
   </td>
   <td>Username used to log in
   </td>
  </tr>
  <tr>
   <td>password
   </td>
   <td>String
   </td>
   <td>Password used to log in
   </td>
  </tr>
  <tr>
   <td>email
   </td>
   <td>String
   </td>
   <td>Email associated with account
   </td>
  </tr>
  <tr>
   <td>groups
   </td>
   <td>Array of Pointers to Group objectIds
   </td>
   <td>List of groups the user is in
   </td>
  </tr>
  <tr>
   <td>createdAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is created (default field)
   </td>
  </tr>
  <tr>
   <td>updatedAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is last updated (default field)
   </td>
  </tr>
</table>


**Model: Group**


<table>
  <tr>
   <td><strong>Property</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>objectId
   </td>
   <td>String
   </td>
   <td>Unique id for the group (default field)
   </td>
  </tr>
  <tr>
   <td>users
   </td>
   <td>Array of Pointers to Users
   </td>
   <td>List of users included in the group
   </td>
  </tr>
  <tr>
   <td>locationCandidates
   </td>
   <td>Array of Pointers to Locations
   </td>
   <td>List of possible locations chosen by users in the group
   </td>
  </tr>
  <tr>
   <td>rankings
   </td>
   <td>JSON Object/map
   </td>
   <td>List of the rankings of each user
   </td>
  </tr>
  <tr>
   <td>status
   </td>
   <td>Number
   </td>
   <td>Status of group - 0 for entering locations, 1 for selecting locations, 2 for voting, 3 for completed
   </td>
  </tr>
  <tr>
   <td>userStatuses
   </td>
   <td>JSON Object/map
   </td>
   <td>List of if user has completed the task for the current status
   </td>
  </tr>
  <tr>
   <td>centralLocation
   </td>
   <td>Pointer to Location
   </td>
   <td>Calculated central location of the group
   </td>
  </tr>
  <tr>
   <td>finalLocation
   </td>
   <td>Pointer to Location
   </td>
   <td>Final location selected by the group
   </td>
  </tr>
  <tr>
   <td>createdAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is created (default field)
   </td>
  </tr>
  <tr>
   <td>updatedAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is last updated (default field)
   </td>
  </tr>
</table>


**Model: Location**


<table>
  <tr>
   <td><strong>Property</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>objectId
   </td>
   <td>String
   </td>
   <td>Unique id for the location (default field)
   </td>
  </tr>
  <tr>
   <td>lat
   </td>
   <td>Number
   </td>
   <td>Latitude of the location
   </td>
  </tr>
  <tr>
   <td>lon
   </td>
   <td>Number
   </td>
   <td>Longitude of the location
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>String
   </td>
   <td>Name of the location
   </td>
  </tr>
  <tr>
   <td>description
   </td>
   <td>String
   </td>
   <td>User entered description
   </td>
  </tr>
  <tr>
   <td>address
   </td>
   <td>String
   </td>
   <td>Address of the location
   </td>
  </tr>
  <tr>
   <td>city
   </td>
   <td>String
   </td>
   <td>City of the location
   </td>
  </tr>
  <tr>
   <td>state
   </td>
   <td>String
   </td>
   <td>State of the location
   </td>
  </tr>
  <tr>
   <td>postalCode
   </td>
   <td>String
   </td>
   <td>Zip code of the location
   </td>
  </tr>
  <tr>
   <td>country
   </td>
   <td>String
   </td>
   <td>Country of the location
   </td>
  </tr>
  <tr>
   <td>group
   </td>
   <td>Pointer to Group
   </td>
   <td>Group the location is relevant to
   </td>
  </tr>
  <tr>
   <td>addedBy
   </td>
   <td>Pointer to User
   </td>
   <td>User who added the location to the candidates
   </td>
  </tr>
  <tr>
   <td>createdAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is created (default field)
   </td>
  </tr>
  <tr>
   <td>updatedAt
   </td>
   <td>DateTime
   </td>
   <td>Date when post is last updated (default field)
   </td>
  </tr>
</table>

  
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
