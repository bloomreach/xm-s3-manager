import React, {Fragment} from 'react';
import ListItem from "@material-ui/core/ListItem";
import Button from "@material-ui/core/Button";
import List from "@material-ui/core/List";
import AttachmentIcon from "@material-ui/icons/Attachment";
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd";
import Chip from "@material-ui/core/Chip";
import {ACLConsumer} from "./ACLContext";

function isEmpty (val) {
  return (val === undefined || val == null || val.length <= 0) ? true : false;
}

const reorder = (list, startIndex, endIndex) => {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);

  return result;
};

class BrXMAppWrapper extends React.Component {

  constructor (props) {
    super(props);

    this.ui = props.ui;
    this.onDelete = this.onDelete.bind(this);
    this.save = this.save.bind(this);
    this.state = {
      items: [],
      mode: 'view'
    };
    this.onDragEnd = this.onDragEnd.bind(this);
  }

  componentDidMount () {
    this.setInitialState(this.ui)
      .then(items => isEmpty(items) ? this.setState({items: []}) : this.setState({items: items}));
  }

  async setInitialState (ui) {
    try {
      const brDocument = await ui.document.get();
      this.mode = brDocument.mode;
      this.setState({mode: this.mode});

      const value = await ui.document.field.getValue();
      return JSON.parse(value);
    } catch (error) {
      console.error('Failed to register extension:', error.message);
      console.error('- error code:', error.code);
    }
  }

  async onDelete (file) {
    await this.setState((state) => {
      const items = state.items.filter(value => value.id !== file.id);
      return {
        items
      };
    });
    this.save();
  }

  save () {
    const items = JSON.stringify(this.state.items);
    this.ui.document.field.setValue(items);
  }

  async openDialog () {
    try {
      const extensionConfig = JSON.parse(this.ui.extension.config);
      this.dialogOptions = {
        title: extensionConfig.title,
        url: './index.html#/dialog',
        size: extensionConfig.size,
        value: JSON.stringify(this.state.items)
      };

      const response = await this.ui.dialog.open(this.dialogOptions);

      this.setState({items: response});
      const items = JSON.stringify(response);
      await this.ui.document.field.setValue(items);
    } catch (error) {
      if (error.code === 'DialogCanceled') {
        return;
      }
      console.error('Error after open dialog: ', error.code, error.message);
    }

  }

  onDragEnd (result) {
    // dropped outside the list
    if (!result.destination) {
      return;
    }

    const items = reorder(
      this.state.items,
      result.source.index,
      result.destination.index
    );

    this.setState({
      items
    });
    this.ui.document.field.setValue(JSON.stringify(items));
  }

  render () {
    const {items} = this.state || [];
    const {mode} = this.state || 'view';
    return <ACLConsumer>{props =>
      props?.useAllowed &&
      <Fragment>
      <Button variant="contained" disabled={mode !== 'edit'} onClick={event => this.openDialog()}>Manage</Button>
      {mode === 'edit' ?
        <DragDropContext onDragEnd={this.onDragEnd}>
          <Droppable droppableId="droppable">
            {(provided, snapshot) => (
              <List
                {...provided.droppableProps}
                ref={provided.innerRef}>
                {items.map((file, index) => (
                  <Draggable key={file.id} draggableId={file.id} index={index}>
                    {(provided, snapshot) => (
                      <ListItem>
                        <Chip ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps} key={index}
                              size={'large'}
                              variant="outlined"
                              icon={<AttachmentIcon/>}
                              label={file.name}
                              onClick={() => window.open(file.link, "_blank")}
                              onDelete={() => this.onDelete(file)}/>
                      </ListItem>
                    )}
                  </Draggable>
                ))}
                {provided.placeholder}
              </List>
            )}
          </Droppable>
        </DragDropContext> :
        <List>
          {items.map((file, index) => (
            <ListItem>
              <Chip key={index}
                    size={'large'}
                    variant="outlined"
                    icon={<AttachmentIcon/>}
                    label={file.name}
                    onClick={() => window.open(file.link, "_blank")}
              />
            </ListItem>
          ))}
        </List>
      }
    </Fragment>
    }
    </ACLConsumer>
  }

}

export default BrXMAppWrapper;