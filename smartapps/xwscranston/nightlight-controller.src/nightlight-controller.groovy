/**
 *  Nightlight Controller
 *
 *  Copyright 2017 William Cranston
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Nightlight Controller",
    namespace: "xwscranston",
    author: "William Cranston",
    description: "Control a dimmable nightlight based on main light status and time of day (after sunset and before sunrise)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("The main light, i.e. the light which controls") {
		// TODO: put inputs here
        input "mainlight", "capability.switch", required: true, title: "Main light?"
	}
    section("The nightlight which is controlled") {
    	input "nightlight", "capability.switchLevel", required: true, title: "Night light?"
    }
}



def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(mainlight, "switch", mainLightHandler)
    subscribe(location, "sunset", sunsetHandler)
    subscribe(location, "sunrise", sunriseHandler)
}

def mainLightHandler(evt) {
    log.debug "mainLightHandler called with mainlight.switch: $evt"
    
    // if switch was turned off, then turn the nightlight on after sunset or before sunrise
    // if switch was turned on, then turn the nightlight off
    if (mainlight.currentSwitch == "off") {
        log.debug "   mainlight turned off"
        if (now() < getSunriseAndSunset().sunrise.time
            || now() > getSunriseAndSunset().sunset.time) {
            log.debug "        nighttime - turn nightlight off"
            nightlight.setLevel(1);
        }
        else {
            log.debug "        daytime - leave nightlight as is"
        }
    }
    else {
        log.debug "    mainlight turned on - turn nightlight off"
        nightlight.setLevel(0)
    }     
}

def sunsetHandler(evt) {
    // turn on at sunrise, but only if the mainlight is off
    log.debug "sunsetHandler called: $evt"
    if (mainlight.currentSwitch == "off") {
        nightlight.setLevel(1)
    }
}

def sunriseHandler(evt) {
    // always turn off at sunrise
    log.debug "sunriseHandler called: $evt"
    
    nightlight.off()
}



// TODO: implement event handlers