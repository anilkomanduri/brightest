/*
 * Copyright 2005 Shinya Kasatani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function Debugger(editor) {
	this.log = new Log("Debugger");
	this.editor = editor;
    this.pauseTimeout = 3000;
	var self = this;
	
	this.init = function() {
		if (this.runner != null) {
			// already initialized
			return;
		}
		
		this.log.debug("init");
        
        this.setState(Debugger.STOPPED);
        
		this.runner = new Object();
		this.runner.editor = this.editor;
        this.runner.setState = function(state) {
            self.setState(state);
        }
        this.editor.app.addObserver({
                testCaseChanged: function(testCase) {
                    self.runner.LOG.info("Changed test case");
                    self.runner.testCase = testCase;
                }
            });
		this.runner.testCase = this.editor.getTestCase();
		
		const subScriptLoader = Components.classes["@mozilla.org/moz/jssubscript-loader;1"]
	    .getService(Components.interfaces.mozIJSSubScriptLoader);
		//subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/selenium-logging.js', this.runner);

		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/scripts/selenium-api.js', this.runner);
		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/scripts/selenium-commandhandlers.js', this.runner);
		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/scripts/selenium-executionloop.js', this.runner);
		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/scripts/selenium-browserbot.js', this.runner);
		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium/scripts/selenium-testrunner.js', this.runner);
    	if (this.editor.getOptions().enableUIElement == 'true') {
            ExtensionsLoader.loadSubScript(subScriptLoader,
                'chrome://selenium-ide/content/ui-element.js', this.runner);
        }
		if (this.editor.getOptions().userExtensionsURL) {
			try {
				ExtensionsLoader.loadSubScript(subScriptLoader, this.editor.getOptions().userExtensionsURL, this.runner);
			} catch (error) {
				this.log.error("error loading user-extensions.js: " + error);
			}
		}
		subScriptLoader.loadSubScript('chrome://selenium-ide/content/selenium-runner.js', this.runner);

        this.editor.infoPanel.logView.setLog(this.runner.LOG);
        
		this.runner.getInterval = function() {
			if (self.runner.testCase.debugContext.currentCommand().breakpoint) {
                self.setState(Debugger.PAUSED);
				return -1;
			} else if (self.state == Debugger.PAUSED || self.state == Debugger.PAUSE_REQUESTED || self.stepContinue) {
                self.stepContinue = false;
                self.setState(Debugger.PAUSED);
				return -1;
			} else {
                return self.editor.getInterval();
			}
		}

        this.runner.shouldAbortCurrentCommand = function() {
            if (self.state == Debugger.PAUSE_REQUESTED) {
                if ((new Date()).getTime() >= self.pauseTimeLimit) {
                    self.setState(Debugger.PAUSED);
                    return true;
                }
            }
            
            return false;
        }
        //AN: move to debugger-extension.js
        this.runner.getDSLCommand = function(command) {
            if (command.target === null || command.target === "") {
                var commandName = command.command;
                var dslCommand = this.editor.getDSLManager().getCommand(commandName);
                if (dslCommand) {
                    return dslCommand;
                }
            }
            return null;
        }
	}
}

Debugger.STATES = defineEnum(Debugger, ["STOPPED", "PLAYING", "PAUSE_REQUESTED", "PAUSED", "STOP_REQUESTED"]);

Debugger.prototype.setState = function(state) {
    this.log.debug("setState: state changed from " + Debugger.STATES[this.state] + " to " + Debugger.STATES[state]);
    this.state = state;
    this.notify("stateUpdated", state);
}

Debugger.prototype.getLog = function() {
    this.init();
    return this.runner.LOG;
}

Debugger.prototype.start = function(complete, useLastWindow) {
	document.getElementById("record-button").checked = false;
	this.editor.toggleRecordingEnabled(false);

	this.log.debug("start");

    this.init();
    var self = this;
    this.setState(Debugger.PLAYING);
//    var watchDog = new WatchDog(this);
	this.runner.start(this.editor.getBaseURL(), {
            testComplete: function(failed) {
                self.setState(Debugger.STOPPED);
                //self.editor.view.rowUpdated(self.runner.testCase.debugContext.debugIndex);
                if (complete) {
                    try {
                        complete(failed);
                    } catch (error) {
                        self.log.error("error at the end of test case: " + error);
                    }
                }
            }
        }, useLastWindow);
};

Debugger.prototype.executeCommand = function(command) {
	document.getElementById("record-button").checked = false;
	this.editor.toggleRecordingEnabled(false);

	this.init();
    if (this.state != Debugger.PLAYING && this.state != Debugger.PAUSE_REQUESTED) {
        this.runner.executeCommand(this.editor.getBaseURL(), command);
    }
};

Debugger.prototype.pause = function() {
	this.log.debug("pause");
    this.setState(Debugger.PAUSE_REQUESTED);
    this.pauseTimeLimit = (new Date()).getTime() + this.pauseTimeout; // 1 second
}

Debugger.prototype.doContinue = function(step) {
	document.getElementById("record-button").checked = false;
	this.editor.toggleRecordingEnabled(false);

	this.log.debug("doContinue: pause=" + step);
	this.init();
    this.stepContinue = step;
    this.setState(Debugger.PLAYING);
    this.runner.continueCurrentTest();
};

Debugger.prototype.showElement = function(locator) {
	this.init();
	this.runner.showElement(locator);
}

//Defining the doStop functionality that will be wired to the Stop button
Debugger.prototype.doStop = function() {
	this.log.debug("stop");
    this.setState(Debugger.STOP_REQUESTED);
};

observable(Debugger);

