/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
  "use strict";

  function iterate (object, func) {
    var property;
    for (property in object) {
      if (object.hasOwnProperty(property)) {
        func(property, object[property]);
      }
    }
  }

  function isEmpty (value) {
    return value === undefined || value === null || value === "";
  }

  function getNestedValue(obj, path) {
    path = path.split('.');
    var value = obj;
    for (var i = 0; i < path.length; i++) {
      value = value[path[i]];
      if (isEmpty(value)) return null;
    }
    return value;
  }

  function setElementAttributes (element, attributeParameterMap, parameters) {
    iterate(attributeParameterMap, function (attribute, parameterName) {
      var parameterValue = getNestedValue(parameters, parameterName);
      if (isEmpty(parameterValue)) {
        element.removeAttribute(attribute);
      } else {
        element.setAttribute(attribute, parameterValue);
      }
    });
  }

  function initS3Manager (editor) {

    var IMAGE_ATTRIBUTE_PARAMETER_MAP = {
        'data-bid': 'id',
        alt: 'description',
        src: 'urlSelectedVariant'
      },
      LANG = editor.lang.s3manager;

    function openImagePickerDialog () {
      editor.openDialog('pickerDialog');
      var pickerDialog = window.CKEDITOR.dialog.getCurrent();
      pickerDialog.parts.footer.hide();
      pickerDialog.on("assetSelected", assetListener);
    }

    editor.ui.addButton('S3Manager', {
      label: LANG.imageTooltip,
      command: 'pickS3Asset',
      toolbar: 'links,10',
      icon: 'plugins/s3manager/icons/s3manager.png',
      allowedContent: 'img[!data-btype,!data-bid,!src,alt]',
      requiredContent: 'img[!data-btype,!data-bid,!src]'

    });

    editor.addCommand('pickS3Asset', {
      exec: function () {
        openImagePickerDialog();
      }
    });

    function insertS3Asset (parameters) {
      var img = editor.document.createElement('img');
      setElementAttributes(img, IMAGE_ATTRIBUTE_PARAMETER_MAP, parameters);
      editor.insertElement(img);
    }

    var assetListener = function (event) {
      console.log(event);
      insertS3Asset(event.data);
    };

  }

  CKEDITOR.plugins.add('s3manager', {
    requires: ['iframe'],
    icons: 's3manager',
    hidpi: false,
    lang: 'en',

    init: function (editor) {
      CKEDITOR.dialog.addIframe('pickerDialog', 'Select S3 asset', this.path.replace('ckeditor/optimized/plugins/s3manager/', 'angular/s3manager/index.html#/ckeditor'), 900, 600, function () { /*oniframeload*/});
      initS3Manager(editor);
    }

  });
}());