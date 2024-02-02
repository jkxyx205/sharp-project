CKEDITOR.plugins.add( 'letterspacing', {
    requires: ['richcombo'],
    init: function( editor ) {
        var config = editor.config,
            lang = editor.lang.format;
        var trackings = [];

        config.allowedContent= 'span'; //There may be a better way to do this.

        for (var i = 1; i <= 10; i++) {
          trackings.push(String(i) + 'px');
        }

        editor.ui.addRichCombo('letterspacing', {
          label: '字间距',
          title: 'Change letter-spacing',
          voiceLabel: 'Change letter-spacing',
          className: 'cke_format',
          multiSelect: false,
            toolbar:"styles,50",

            panel: {
            css : [ config.contentsCss, CKEDITOR.getUrl( CKEDITOR.skin.getPath('editor') + 'editor.css' ) ]
          },

          init: function() {
            this.startGroup('字间距');
            for (var this_letting in trackings) {
              this.add(trackings[this_letting], trackings[this_letting], trackings[this_letting]);
            }
          },

          onClick: function(value) {
            editor.focus();
            editor.fire('saveSnapshot');
            var ep = editor.elementPath();
            var style = new CKEDITOR.style({styles: {'letter-spacing': value}});
            editor[style.checkActive(ep) ? 'removeStyle' : 'applyStyle' ](style);

            editor.fire('saveSnapshot');
          }
        });
    }
});
