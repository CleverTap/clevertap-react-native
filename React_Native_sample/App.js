import * as React from 'react';
import { Button, View } from 'react-native';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { NavigationContainer } from '@react-navigation/native';
import { enableScreens } from 'react-native-screens';
//import all the components we are going to use
import {
  SafeAreaView,
  StyleSheet,
  Text,
  Image,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { useState, useEffect } from 'react';
enableScreens();
const CleverTap = require('clevertap-react-native');
//// Function Props

userProfile = () => {
			alert('User Profile Updated');
	
		CleverTap.profileSet({"Identity":451890, "Name":"H","Email":"h@gmail.com","Gender":"Male","DOB":"1995-10-14", "custom":1.73});
								
		}; 
TouchableOpacity.defaultProps = { activeOpacity: 0.8 };	
// Drawer components
function User_Properties({ navigation }) {
	
	const [showText, setShowText] = useState(true);

  useEffect(() => {
    // Change the state every second or the time given by User.
    const interval = setInterval(() => {
      setShowText((showText) => !showText);
    }, 1000);
    return () => clearInterval(interval);
  }, []);
	
  return (
    <SafeAreaView style={{ flex: 1 }}>
 <View>
      <TouchableOpacity onPress={this.userProfile}>
        <Image
          source={{
            uri:
              'https://github.com/Rashmi-9514/React-Native/blob/master/user%20image.png?raw=true',
          }}
          style={styles.image}
        />
      </TouchableOpacity>
	  </View>
	
      <View style={styles.container}>
        <Text
          style={[styles.textStyle, { display: showText ? 'none' : 'flex' }]}>
          Click On image to Set User Profile
        </Text>
      </View>
	        
   
    </SafeAreaView>
   );
}

//Identity_Management
id_mngmt = () => {
			alert('User Profile Updated');
	
		//On user Login 
		CleverTap.onUserLogin({'Name': 'h', 'Identity': '45670', 'Email': 'h@gmail.com', 'custom1': 13});
								
		}; 
rmvalskey = () => {
			alert('User Profile Updated');
	
		//Removing Multiple Values
		CleverTap.profileRemoveMultiValuesForKey(['a', 'c'], 'letters');
								
		}; 
rmvalkey = () => {
			alert('User Profile Updated');
	
		//Removing Value for key
		CleverTap.profileRemoveValueForKey("letters");
								
		}; 
getCTid = () =>{
	
	CleverTap.profileGetCleverTapID((err, res) => {
           		 console.log('CleverTapID', res, err);
				 alert('Check console for CleverTapID', res,err);
        	});
}

function Identity_Management({ navigation }) {
  return (
  <View>
  <TouchableOpacity
          style={styles.button}
          onPress={this.id_mngmt}
         >
          <Text style={styles.btnText}>On User Login
			</Text>
</TouchableOpacity>

  <TouchableOpacity
          style={styles.button}
          onPress={this.rmvalskey}
         >
          <Text style={styles.btnText}>Remove Multi Value For Key
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.rmvalkey}
         >
          <Text style={styles.btnText}>removeValueForKey
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.getCTid}
         >
          <Text style={styles.btnText}>getCleverTapID
			</Text>
</TouchableOpacity>
</View>
  );
}

// Location
userLocation = () => {
			alert('User Location set');
	
		CleverTap.setLocation(34.15, -118.20);
								
		}; 

function Location({ navigation }) {
	const [showText, setShowText] = useState(true);

  useEffect(() => {
    // Change the state every second or the time given by User.
    const interval = setInterval(() => {
      setShowText((showText) => !showText);
    }, 1000);
    return () => clearInterval(interval);
  }, []);
	
  return (
  <SafeAreaView style={{ flex: 1 }}>
 <View>
      <TouchableOpacity onPress={this.userLocation}>
        <Image
          source={{
            uri:
              'https://github.com/Rashmi-9514/React-Native/blob/master/flat_location_logo.png?raw=true',
          }}
          style={styles.image}
        />
      </TouchableOpacity>
	  </View>
	
      <View style={styles.container}>
        <Text
          style={[styles.textStyle, { display: showText ? 'none' : 'flex' }]}>
          Click On image to set Location
        </Text>
      </View>
    </SafeAreaView>
   );
}

///Events

pushevent = () => {
			alert('Event Recorded');
	
		//Recording an Event	
		CleverTap.recordEvent('testEvent');
								
		}; 

pushchargedevent = () => {
			alert('Charged Event Recorded');
	
		//Recording an Event	
		CleverTap.recordChargedEvent({'totalValue': 20, 'category': 'books'}, [{'title': 'book1'}, {'title': 'book2'}, {'title': 'book3'}]);
								
		}; 
function Events({ navigation }) {
  return (
    <View>
  <TouchableOpacity
          style={styles.button}
          onPress={this.pushevent}
         >
          <Text style={styles.btnText}>Push Event
			</Text>
</TouchableOpacity>

  <TouchableOpacity
          style={styles.button}
          onPress={this.pushchargedevent}
         >
          <Text style={styles.btnText}>Push Charged Event
			</Text>
</TouchableOpacity>
</View>
  );
}

//App Inbox

appInbox = () => {
			alert('I am an alert for on button click');
  
		//console.log('Display on called: ', res, err);
			
		
		//Show Inbox 
		CleverTap.showInbox({'tabs':['Offers','Promotions'],'navBarTitle':'My App Inbox','navBarTitleColor':'#FF0000','navBarColor':'#FFFFFF','inboxBackgroundColor':'#AED6F1','backButtonColor':'#00FF00'
                                ,'unselectedTabColor':'#0000FF','selectedTabColor':'#FF0000','selectedTabIndicatorColor':'#000000',
                                'noMessageText':'No message(s)','noMessageTextColor':'#FF0000'});
								
								
		}; 		

getTotmsg = () => {
			//Get Total messagecount
			alert('Check Console for values');
		CleverTap.getInboxMessageCount((err, res) => {
				console.log('Total Messages: ', res, err);
			});							
		}; 
unread = () => {
		alert('Check Console for values');
			//Get the count of unread messages
		CleverTap.getInboxMessageUnreadCount((err, res) => {
				console.log('Unread Messages: ', res, err);
			});							
		}; 	
allmsg = () => {
			alert('Check Console for values');
			//Get All Inbox Messages
		CleverTap.getAllInboxMessages((err, res) => {
				console.log('All Inbox Messages: ', res, err);
			 });							
		}; 
allunreadmsg = () => {
			alert('Check Console for values');
		//get all Inbox unread messages
		CleverTap.getUnreadInboxMessages((err, res) => {
				 console.log('Unread Inbox Messages: ', res, err);
			 });			
		}; 
inboxid = () => {
			//Get inbox Id
			alert('Check Console for values');
		CleverTap.getInboxMessageForId('Message Id',(err, res) => {
            		console.log("marking message read = "+res);
        	});							
		}; 	 

deleteMsg = () => {
			//Get inbox Id
			alert('Check Console for values');
		CleverTap.deleteInboxMessageForId('Message Id');
        							
		}; 	

markread = () => {
			//Get inbox Id
			alert('Check Console for values');
		CleverTap.markReadInboxMessageForId('Message Id');
        							
		}; 
pnviewed = () => {
			//Get inbox Id
			alert('Check Console for values');
		CleverTap.pushInboxNotificationViewedEventForId('Message Id');
        							
		}; 
pnclicked = () => {
			//Get inbox Id
			alert('Check Console for values');
		CleverTap.pushInboxNotificationClickedEventForId('Message Id');	
        								
		}; 


function App_Inbox({ navigation }) {
  return (
     <View>
<TouchableOpacity
          style={styles.button}
          onPress={this.appInbox}
         >
          <Text style={styles.btnText}>show App Inbox
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.getTotmsg}
         >
          <Text style={styles.btnText}>get Inbox Message Count
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.unread}
         >
          <Text style={styles.btnText}>get Inbox Message Unread Count
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.allmsg}
         >
          <Text style={styles.btnText}>get All Inbox Messages
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.allmsg}
         >
          <Text style={styles.btnText}>get All Inbox Messages
			</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.allunreadmsg}
         >
          <Text style={styles.btnText}>get Unread Inbox Messages
		  </Text>
</TouchableOpacity>


<TouchableOpacity
          style={styles.button}
          onPress={this.inboxid}
         >
          <Text style={styles.btnText}>get Inbox Message For Id
		  </Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.deleteMsg}
         >
          <Text style={styles.btnText}>Todelete Inbox Message
		  </Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.markread}
         >
          <Text style={styles.btnText}>mark Read Inbox Message
		  </Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.pnviewed}
         >
          <Text style={styles.btnText}>pushInboxNotificationViewedEvent
		</Text>
</TouchableOpacity>

<TouchableOpacity
          style={styles.button}
          onPress={this.pnclicked}
         >
          <Text style={styles.btnText}>pushInboxNotificationClickedEvent
		  </Text>
</TouchableOpacity>

</View>
  );
}


function Enable_Debugging({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}
function Push_Notifications({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}
function Native_Display({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}


function Product_Config({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

function Feature_Flag({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

function Attribution({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

function App_Personalisation({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

function GDPR({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

function Multi_instance({ navigation }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button onPress={() => navigation.goBack()} title="Go back home" />
    </View>
  );
}

const Drawer = createDrawerNavigator();

export default function App() {
	CleverTap.setDebugLevel(3);
	CleverTap.initializeInbox(); 
			
  return (
  
    <NavigationContainer>
      <Drawer.Navigator initialRouteName="Home">
        <Drawer.Screen name="User Properties" component={User_Properties} />
        <Drawer.Screen name="Identity Management" component={Identity_Management} />
		<Drawer.Screen name="Location" component={Location} />
		<Drawer.Screen name="Events" component={Events} />
		<Drawer.Screen name="App Inbox" component={App_Inbox} />		
		<Drawer.Screen name="Enable Debugging" component={Enable_Debugging} />
		<Drawer.Screen name="Push Notifications" component={Push_Notifications} />
		<Drawer.Screen name="Native Display" component={Native_Display} />
		<Drawer.Screen name="Product Config" component={Product_Config} />
		<Drawer.Screen name="Feature Flag" component={Feature_Flag} />
		<Drawer.Screen name="App Personalisation" component={App_Personalisation} />
		<Drawer.Screen name="Attribution" component={Attribution} />
		<Drawer.Screen name="GDPR" component={GDPR} />
		<Drawer.Screen name="Multi-instance" component={Multi_instance} />
      </Drawer.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    margin: 10,
    marginTop: 50,
    padding: 10,
  },
buttonFacebookStyle: {
    flexDirection: 'column',
    alignItems: 'center',
    backgroundColor: '#f4c2c2',
    borderWidth: 100,
    borderColor: '#fff',
    height: 100,
    borderRadius: 100,
    margin: 100,
  },
  buttonImageIconStyle: {
    padding: 2,
    margin: -60,
    height: 90,
    width: 95,
    resizeMode: 'stretch',
  },
  image: {
	padding: 50,
	margin: 100,
    marginTop: 200,
    height: 200,
    width: 200,
  },
  buttonTextStyle: {
    color: '#fff',
    marginBottom: -30,
    marginLeft: 10,
  },
  buttonIconSeparatorStyle: {
    backgroundColor: '#fff',
    width: 20,
    height: 40,
  },
  titleText: {
    fontSize: 22,
    textAlign: 'center',
    fontWeight: 'bold',
  },
  textStyle: {
    textAlign: 'center',
    marginTop: 10,
  },
  appButtonContainer: {
    elevation: 8,
    backgroundColor: "#009688",
    borderRadius: 10,
    paddingVertical: 10,
    paddingHorizontal: 12
  },
  appButtonText: {
    fontSize: 18,
    color: "#fff",
    fontWeight: "bold",
    alignSelf: "center",
    textTransform: "uppercase"
  },
  button: {
    width: 400,
    marginTop: 20,
    backgroundColor: "green",
    padding: 15,
    borderRadius: 50,
  },
  btnText: {
    color: "white",
    fontSize: 20,
    justifyContent: "center",
    textAlign: "center",
  },

});