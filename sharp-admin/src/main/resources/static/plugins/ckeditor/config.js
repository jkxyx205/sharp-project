/**
 * @license Copyright (c) 2003-2019, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

CKEDITOR.editorConfig = function( config ) {
    config.language = 'zh-cn';
    //remove button
    config.removePlugins = 'elementspath';
    config.resize_enabled = false;

    config.filebrowserImageUploadUrl= "/ckeditor/uploadImage";

    config.extraPlugins = 'letterspacing,file,lineheight';

};
