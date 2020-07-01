import React from 'react';
import S3Explorer from "./S3Explorer";

function isEmpty (val) {
  return (val === undefined || val == null || val.length <= 0) ? true : false;
}

class BrXMExplorerDialogWrapper extends React.Component {

  constructor (props) {
    super(props);

    this.onClose = this.onClose.bind(this);
    this.ui = props.ui;
    this.extensionConfig = JSON.parse(this.ui.extension.config);
    this.baseURL = this.ui.baseUrl+this.extensionConfig.baseURL;
    this.authenticationHeader = this.extensionConfig.authenticationHeader;
    this.state = {
      items: [],
      loaded: false
    }
  }

  componentDidMount () {
    this.setInitialState()
      .then(items => isEmpty(items) ? this.setState({items: []}) : this.setState({items: items}))
      .then(() => this.setState({loaded: true}));
  }

  async setInitialState () {
    try {
      const options = await this.ui.dialog.options();
      const items = JSON.parse(options.value);
      return items;
    } catch (error) {
      console.error('Failed to register extension:', error.message);
      console.error('- error code:', error.code);
    }
  }

  onClose (items) {
    this.ui.dialog.close(items);
  }

  render () {
    const {items} = this.state || [];
    const {loaded} = this.state || false;
    return loaded ? <S3Explorer onClose={this.onClose} selected={items} baseURL={this.baseURL}/> : ''
  }

}

export default BrXMExplorerDialogWrapper;