/*
 * Copyright 2020-2025 Bloomreach (http://www.bloomreach.com)
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

    function iterate(object, func) {
        var property;
        for (property in object) {
            if (object.hasOwnProperty(property)) {
                func(property, object[property]);
            }
        }
    }

    function isEmpty(value) {
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

    function isSelectionEmpty(selection) {
        if (selection === null || selection.getType() === CKEDITOR.SELECTION_NONE) {
            return true;
        }
        var ranges = selection.getRanges();
        return ranges.length === 0 || ranges[0].collapsed;
    }

    function getSelectedLinkOrNull(selection) {
        var startElement, linkNode;

        if (selection === null) {
            return null;
        }

        startElement = selection.getStartElement();

        if (startElement !== null) {
            linkNode = startElement.getAscendant('a', true);
            if (linkNode !== null && linkNode.is('a')) {
                return linkNode;
            }
        }
        return null;
    }

    function createLinkAttributePairs(paramsMap, parameters) {
        var pairs = {};
        iterate(paramsMap, function (attribute, parameterName) {
            var parameterValue = parameters[parameterName];
            if (parameterValue !== undefined && parameterValue !== null && parameterValue !== "") {
                pairs[attribute] = parameterValue;
            }
        });
        return pairs;
    }

    function setElementAttributes(element, attributeParameterMap, parameters) {
        iterate(attributeParameterMap, function (attribute, parameterName) {
            var parameterValue = getNestedValue(parameters, parameterName);
            if (isEmpty(parameterValue)) {
                element.removeAttribute(attribute);
            } else {
                element.setAttribute(attribute, parameterValue);
            }
        });
    }

    function initS3Manager(editor) {

        var IMAGE_ATTRIBUTE_PARAMETER_MAP = {
                'data-s3id': 'id',
                alt: 'name',
                src: 'link'
            },
            LINK_ATTRIBUTES_PARAMETER_MAP = {
                'data-s3id': 'id',
                href: 'link'
            },
            LANG = editor.lang.s3manager;

        function openImagePickerDialog() {
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
            allowedContent: 'img[!data-s3id,!src,alt]; a[!data-s3id,!href]',
            requiredContent: 'img[!data-s3id,!src]; a[!data-s3id,!href]'
        });

        editor.addCommand('pickS3Asset', {
            exec: function () {
                openImagePickerDialog();
            }
        });

        function insertS3Asset(parameters) {
            var selection = editor.getSelection();

            if (isSelectionEmpty(selection)) {
                insertS3Image(parameters);
            } else {
                insertS3Link(parameters);
            }
        }

        function createLinkFromSelection(selection, linkParameters) {
            var range, linkAttributes, linkStyle;

            range = selection.getRanges()[0];

            linkAttributes = createLinkAttributePairs(LINK_ATTRIBUTES_PARAMETER_MAP, linkParameters);
            linkStyle = new CKEDITOR.style({
                element: 'a',
                attributes: linkAttributes,
                type: CKEDITOR.STYLE_INLINE
            });
            linkStyle.applyToRange(range);
            range.select();
        }

        function insertS3Link(parameters) {
            var selectedLink = getSelectedLinkOrNull(editor.getSelection());

            if (selectedLink !== null) {
                setElementAttributes(selectedLink, LINK_ATTRIBUTES_PARAMETER_MAP, parameters);

                // ensure compatibility with the 'link' plugin, which creates an additional attribute
                // 'data-cke-saved-href' for each link that overrides the actual href value. We don't need
                // this attribute, so remove it.
                selectedLink.removeAttribute('data-cke-saved-href');
            } else {
                createLinkFromSelection(editor.getSelection(), parameters);
                selectedLink = getSelectedLinkOrNull(editor.getSelection());
            }

            if (selectedLink !== null) {
                if (selectedLink.hasAttribute('data-s3id')) {
                    // Make sure link is being updated
                    editor.updateElement();
                } else {
                    // the link has been removed in the picker dialog
                    selectedLink.remove(true);
                }
            }
        }

        function insertS3Image(parameters) {
            var img = editor.document.createElement('img');
            setElementAttributes(img, IMAGE_ATTRIBUTE_PARAMETER_MAP, parameters);
            editor.insertElement(img);
        }

        var assetListener = function (event) {
            if (event.data && event.data.length) {
                insertS3Asset(event.data[0]);
            }
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
