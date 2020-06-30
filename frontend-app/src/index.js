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

const axios = require('axios').default;

function getACL(baseUrl) {
  return axios.get('/acl', {
    baseURL: baseUrl,
  }).then(response => {
    return response.data
  }).catch(exception => {
    console.error(exception);
  });
}

document.addEventListener('DOMContentLoaded', async () => {
  try {

    //TODO Better handling
    let ui;
    let extensionConfig;
    let acl;
    let baseUrl;
    console.log(window.location.href);
    if(!window.location.href.includes('ckeditor')) {
      ui = await UiExtension.register();
      extensionConfig = JSON.parse(ui.extension.config);
      baseUrl = ui.baseUrl+extensionConfig.baseURL;

    } else {
      baseUrl = window.location.href.replace('angular/s3manager/index.html#/ckeditor', 'ws/s3manager/awsS3');
    }
    acl = await getACL(baseUrl);

    const routing = (
      <ACLProvider value={acl}>
        <HashRouter>
          <Switch>
            <Route path="/ckeditor" render={props => <S3Explorer baseURL={'http://localhost:8080/cms/ws/s3manager/awsS3'} context='ckeditor'/>}/>
            <Route path="/dialog" render={props => <BrXMExplorerDialogWrapper ui={ui}/>}/>
            <Route exact path="/" render={props => <BrXMAppWrapper ui={ui}/>}/>
          </Switch>
        </HashRouter>
      </ACLProvider>
    );

    ReactDOM.render(routing, document.getElementById("root"));

  } catch (error) {
    console.log(error);
    console.error('Failed to register extension:', error.message);
    console.error('- error code:', error.code);
    // console.log('fallback render');
    // ReactDOM.render(
    //   <S3Explorer authenticationHeader={'Basic YWRtaW46YWRtaW4='} onClose={(items) => console.log(items)} baseURL={'http://localhost:8080/cms/ws/s3manager/awsS3'}/>, document.getElementById('root')
    // );
  }
});

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
