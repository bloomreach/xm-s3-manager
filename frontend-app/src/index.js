import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import * as serviceWorker from './serviceWorker';
import S3Explorer from "./S3Explorer";
import {HashRouter, Route, Switch} from "react-router-dom";
import * as UiExtension from "@bloomreach/ui-extension";
import BrXMExplorerDialogWrapper from "./BrXMExplorerDialogWrapper";
import BrXMAppWrapper from "./BrXMAppWrapper";
import {ACLProvider} from "./ACLContext";
import {MuiThemeProvider, createMuiTheme} from "@material-ui/core/styles";
import {DZConfProvider} from "./DZConfContext";

const axios = require('axios').default;

const theme = createMuiTheme({
  palette: {
    primary:{
      light: 'rgba(0,0,0,0.05)',
      main: '#147AC8',
      dark: '#1265B3',
    },
    secondary: {
      light: 'rgba(0,0,0,0.05)',
      main: '#147AC8',
      dark: '#1265B3',
    },
  },

});

function getACL(baseUrl) {
  return axios.get('/acl', {
    baseURL: baseUrl,
  }).then(response => {
    return response.data
  }).catch(exception => {
    console.error(exception);
  });
}

function getDZConf(baseUrl) {
  return axios.get('/dzconf', {
    baseURL: baseUrl,
  }).then(response => {
    return response.data
  }).catch(exception => {
    console.error(exception);
  });
}

document.addEventListener('DOMContentLoaded', async () => {
  try {

    let ui;
    let extensionConfig;
    let acl;
    let dzConf;
    let baseUrl;
    if(!window.location.href.includes('ckeditor')) {
      ui = await UiExtension.register();
      extensionConfig = JSON.parse(ui.extension.config);
      baseUrl = ui.baseUrl+extensionConfig.baseURL;

    } else {
      baseUrl = window.location.href.replace('angular/s3manager/index.html#/ckeditor', 'ws/s3manager/awsS3');
    }
    acl = await getACL(baseUrl);
    dzConf = await getDZConf(baseUrl);

    const routing = (
      <MuiThemeProvider theme={theme}>
      <ACLProvider value={acl}>
        <DZConfProvider value={dzConf}>
          <HashRouter>
            <Switch>
              <Route path="/ckeditor" render={props => <S3Explorer baseURL={baseUrl} context='ckeditor'/>}/>
              <Route path="/dialog" render={props => <BrXMExplorerDialogWrapper ui={ui}/>}/>
              <Route exact path="/" render={props => <BrXMAppWrapper ui={ui}/>}/>
            </Switch>
          </HashRouter>
        </DZConfProvider>
      </ACLProvider>
      </MuiThemeProvider>
    );

    ReactDOM.render(routing, document.getElementById("root"));

  } catch (error) {
    console.log(error);
    console.error('Failed to register extension:', error.message);
    console.error('- error code:', error.code);
  }
});

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
