import React, {Fragment} from 'react';
import Dialog from "@material-ui/core/Dialog";
import Button from "@material-ui/core/Button";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText";
import DialogActions from "@material-ui/core/DialogActions";
import CloudUploadIcon from "@material-ui/icons/CloudUpload";
import {UploaderComponent} from '@syncfusion/ej2-react-inputs';
import './S3UploadDialog.css';

class S3UploadDialog extends React.Component {

  constructor (props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);
    this.handleClickOpen = this.handleClickOpen.bind(this);
    this.state = {
      open: false,
      folder: props.folder,
      asyncSettings: {
        saveUrl: props.baseURL + '/uploadFiles?path=' + props.folder,
        chunkSize: (5 * 1024 * 1024)
      }
    }
    this.tableRef = props.table;
  }

  componentDidMount () {
  }

  handleClose () {
    this.setState({open: false});
    this.tableRef.current && this.tableRef.current.onQueryChange();
  }

  handleClickOpen () {
    this.setState({open: true});
  }

  render () {
    const {open} = this.state || false;
    const {asyncSettings} = this.state || false;
    return <Fragment>
      <Button
        onClick={this.handleClickOpen}
        size={'small'}
        variant="contained"
        color="default"
        startIcon={<CloudUploadIcon/>}
        style={{width: '150px'}}
      >
        Upload
      </Button>
      <Dialog open={open} onClose={this.handleClose} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">Upload</DialogTitle>
        <DialogContent>
          <DialogContentText>
            To upload a file larger than 160 GB, use the AWS CLI, AWS SDK, or Amazon S3 REST API.
          </DialogContentText>
          <UploaderComponent maxFileSize={16000000000} autoUpload={false} asyncSettings={asyncSettings}/>
        </DialogContent>
        <DialogActions>
          <Button onClick={this.handleClose} color="primary">
            OK
          </Button>
        </DialogActions>
      </Dialog>
    </Fragment>
  }
}

export default S3UploadDialog;


