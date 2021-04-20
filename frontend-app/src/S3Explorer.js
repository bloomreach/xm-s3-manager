import React from 'react';
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import S3UploadDialog from "./S3UploadDialog";
import MaterialTable, {MTableToolbar} from "material-table";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Link from "@material-ui/core/Link";
import HomeIcon from '@material-ui/icons/Home';
import Button from "@material-ui/core/Button";
import Dialog from "@material-ui/core/Dialog";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import Chip from "@material-ui/core/Chip";
import AddCircleIcon from '@material-ui/icons/AddCircle';
import DescriptionIcon from '@material-ui/icons/Description';
import DialogTitle from "@material-ui/core/DialogTitle";
import TextField from "@material-ui/core/TextField";
import FolderIcon from '@material-ui/icons/Folder';
import ListItemText from "@material-ui/core/ListItemText";
import ListItem from "@material-ui/core/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import Avatar from "@material-ui/core/Avatar";
import {ACLConsumer} from "./ACLContext";

const axios = require('axios').default;

function openInNewTab (url) {
  var win = window.open(url, '_blank', 'noopener,noreferrer');
  if (win){
    win.opener = null;
    win.focus();
  }
}

function isEmpty (val) {
  return (val === undefined || val == null || val.length <= 0) ? true : false;
}

Array.prototype.unique = function () {
  var a = this.concat();
  for (var i = 0; i < a.length; ++i) {
    for (var j = i + 1; j < a.length; ++j) {
      if (a[i] === a[j]) {
        a.splice(j--, 1);
      }
    }
  }

  return a;
};

class S3Explorer extends React.Component {

  constructor (props) {
    super(props);

    this.getItems = this.getItems.bind(this);
    this.updateItems = this.updateItems.bind(this);
    this.selectFolder = this.selectFolder.bind(this);
    this.navigateToFolderAndAddToBreadCrumb = this.navigateToFolderAndAddToBreadCrumb.bind(this);
    this.selectFromBreadCrumb = this.selectFromBreadCrumb.bind(this);
    this.setSelected = this.setSelected.bind(this);
    this.deleteFiles = this.deleteFiles.bind(this);
    this.openCreateFolderDialog = this.openCreateFolderDialog.bind(this);
    this.closeCreateFolderDialog = this.closeCreateFolderDialog.bind(this);
    this.okCreateFolderDialog = this.okCreateFolderDialog.bind(this);

    this.onClose = props.onClose;

    const selected = isEmpty(props.selected) ? [] : props.selected

    this.state = {
      items: [],
      folder: '',
      breadCrumb: [],
      selected: selected,
      query: {
        search: ''
      },
      baseURL: props.baseURL,
      openCreateDialog: false,
      folderName: '',
      currentSelection: []
    }
    this.tableRef = React.createRef();
    this.createFolderRef = React.createRef();
  }

  componentDidMount () {
  }

  updateItems () {
    this.getItems().then(items => this.setState({items: items}));
  }

  navigateToFolderAndAddToBreadCrumb (item) {
    this.setState((state) => {
      const breadCrumb = state.breadCrumb.concat(item);
      return {
        breadCrumb
      };
    })
    this.selectFolder(item.path);
  }

  selectFromBreadCrumb (item) {
    this.setState((state) => {
      const breadCrumb = state.breadCrumb.slice(0, state.breadCrumb.indexOf(item) + 1);
      return {
        breadCrumb
      };
    })
    this.selectFolder(item.path);
  }

  selectHome () {
    this.setState({breadCrumb: []});
    this.selectFolder('');
  }

  setSelected (data) {
    this.setState((state) => {
      const selected = state.selected.concat(data.filter(value => value.type !== 'FOLDER')).unique();
      return {
        selected
      };
    });
  }

  async delete (items) {
    await this.deleteFiles(items);
    items.map((value, index) => this.deleteSelected(value));
    this.selectFolder(this.state.folder);
  }

  async deleteFiles (items) {
    return axios({
      baseURL: this.state.baseURL,
      method: 'DELETE',
      url: '/deleteFiles',
      data: items
    });
  }

  selectFolder (folder) {
    this.setState({folder: folder});
    this.tableRef.current && this.tableRef.current.onQueryChange();
  }

  deleteSelected (item) {
    this.setState((state) => {
      const selected = state.selected.filter(value => value.id !== item.id);
      return {
        selected
      };
    });
  }

  async getItems (query) {
    return axios.get('/list', {
      baseURL: this.state.baseURL,
      params: {
        path: this.state.folder,
        query: query.search
      }
    }).then(response => {
      return response.data
    }).catch(exception => {
      console.error(exception);
    });
  }

  closeCreateFolderDialog () {
    this.setState({openCreateDialog: false});
    this.tableRef.current && this.tableRef.current.onQueryChange();
  }

  async okCreateFolderDialog () {
    await this.createFolder(this.state.folder + this.createFolderRef.current.value);
    this.closeCreateFolderDialog();
  }

  async createFolder (path) {
    return axios({
      baseURL: this.state.baseURL,
      method: 'POST',
      url: '/createFolder',
      data: {path: path}
    });
  }

  openCreateFolderDialog () {
    this.setState({openCreateDialog: true})
  }

  containsObject(obj, list) {
    var x;
    for (x in list) {
      if (list.hasOwnProperty(x) && list[x].id === obj.id) {
        return true;
      }
    }
    return false;
  }

  determineDisabled(obj, list){
    if(this.props.context==='ckeditor' && this.state.currentSelection.length > 0){
      return !this.containsObject(obj, this.state.currentSelection);
    } else if (this.props.context==='ckeditor' && this.state.selected.length > 0) {
      return true;
    } else {
      return this.containsObject(obj, list);
    }
  }

  triggerckeditorEvent(item){
    var pickerDialog = window.parent.CKEDITOR.dialog.getCurrent();
    pickerDialog.fire('assetSelected', item);
    pickerDialog.getButton('cancel').click();
  }

  render () {
    const {openCreateDialog} = this.state || false;
    const {folder} = this.state || '';
    const {breadCrumb} = this.state || [];
    const {selected} = this.state || [];
    return <ACLConsumer>
      {permissions =>
        <Dialog fullScreen open={true}>
          <DialogContent style={{padding: '0 0', marginTop: '110px'}}>
            <MaterialTable

              tableRef={this.tableRef}
              // other props
              title={''}
              columns={[
                {
                  cellStyle: {
                    width: '100%',
                  },
                  title: 'Name',
                  field: 'name',
                  render: (row) =>
                    // <Fragment>
                    <ListItem
                      onClick={(event) => row.type === 'FOLDER' ? this.navigateToFolderAndAddToBreadCrumb(row) : openInNewTab(row.link)}>
                      <ListItemIcon>
                        {row.type === 'FOLDER' ? <FolderIcon/> : row.type === 'IMAGE' ?
                          <Avatar alt={row.name} src={row.link}/> : <DescriptionIcon/>
                        }
                      </ListItemIcon>
                      <ListItemText style={{cursor: 'pointer'}}
                                    primary={row.name}/>
                    </ListItem>
                },
                {title: 'Last Modified', field: 'lastModified'},
                {
                  title: 'Size', field: 'size'
                  , render: (row) =>
                    // <Fragment>
                    <span>{row.humanReadableSize}</span>
                }
              ]}
              data={query =>
                new Promise((resolve, reject) => {
                  // prepare your data and then call resolve like this:
                  this.getItems(query)
                    .then(result => {
                      resolve({
                        data: result,
                        page: 0,
                        totalCount: isEmpty(result) ? 0 : result.length,
                      })
                    })
                })
              }
              options={{
                search: true,
                paging: false,
                selection: true,
                padding: 'dense',
                showSelectAllCheckbox: this.props.context !== 'ckeditor',
                selectionProps: rowData => {
                  rowData.tableData.disabled = this.determineDisabled(rowData, selected);
                  return {
                    disabled: this.determineDisabled(rowData, selected),
                  }
                },
                sorting: false,
              }}
              onSelectionChange={(data) => this.setState({currentSelection:data})}
              components={{
                Toolbar: props => (
                  <AppBar position="fixed" color={'default'}>
                    <MTableToolbar {...props} />
                    <Toolbar variant={'dense'}>
                      <Breadcrumbs aria-label="breadcrumb" style={{width: '100%'}}>
                        <Link color="inherit" onClick={() => this.selectHome()}>
                          <HomeIcon fontSize={'small'}/>
                        </Link>
                        {breadCrumb.map((value, index) =>
                          <Link key={index} color="inherit" id={value.id}
                                onClick={() => this.selectFromBreadCrumb(value)}>
                            {value.name}
                          </Link>
                        )}
                      </Breadcrumbs>
                      {permissions?.createAllowed &&
                        <Button
                          onClick={() => this.openCreateFolderDialog()}
                          size={'small'}
                          style={{width: '265px', marginRight: '15px'}}
                          variant="contained"
                          color="default"
                          startIcon={<AddCircleIcon/>}
                        >
                          Create folder
                        </Button>
                      }
                      <Dialog open={openCreateDialog} onClose={this.closeCreateFolderDialog}
                              aria-labelledby="form-dialog-title">
                        <DialogTitle id="form-dialog-title">Create Folder</DialogTitle>
                        <DialogContent>
                          <TextField autoFocus inputRef={this.createFolderRef} label="Folder name" variant="outlined"/>
                        </DialogContent>
                        <DialogActions>
                          <Button onClick={this.closeCreateFolderDialog} color="primary">
                            Cancel
                          </Button>
                          <Button onClick={this.okCreateFolderDialog} color="primary">
                            OK
                          </Button>
                        </DialogActions>
                      </Dialog>
                      {permissions?.uploadAllowed &&
                        <S3UploadDialog baseURL={this.state.baseURL} table={this.tableRef} folder={folder}/>
                      }
                    </Toolbar>
                  </AppBar>
                )
              }}

              actions={[
                {
                  tooltip: 'Add to selection',
                  icon: 'add_to_photos',
                  onClick: (evt, data) => this.setSelected(data)
                },
                {
                  tooltip: 'Delete',
                  icon: 'delete',
                  onClick: (evt, data) => this.delete(data),
                  hidden: !permissions?.deleteAllowed
                },
              ]}
            />
          </DialogContent>
          <DialogActions>
            <div style={{flex: 5}}>
              {selected.map((value) =>
                <Chip key={value.id}
                      onClick={() => this.deleteSelected(value)}
                      onDelete={() => this.deleteSelected(value)}
                      variant="outlined"
                      icon={<DescriptionIcon/>}
                      size={'small'}
                      title={value.name}
                      label={value.name.replace(/^(.{5}[^\s]*).*/, "$1")}
                />)}
            </div>
            <Button color="inherit" onClick={event => this.props.context==='ckeditor' ? this.triggerckeditorEvent(this.state.selected) : this.onClose(this.state.selected)}>Ok</Button>
          </DialogActions>
        </Dialog>
      }
    </ACLConsumer>
  }

}

export default S3Explorer;


