'use strict';

/* Directives */

angular.module('breizhCampCFP.directives', [])
    .directive('markdownpreview', function($log) {
        // return the directive link function. (compile function not needed)
        return function(scope, element, attrs) {
            var content; // contenu markdown

            // used to update the UI
            function updateContent() {
                if (content !== undefined && content != null) {
                    element.html(scope.converter.makeHtml(content));
                } else {
                    element.text('');
                }
            }

            // watch the expression, and update the UI on change.
            scope.$watch(attrs.markdownpreview, function(value) {
                content = value;
                updateContent();
            });
        }
    });

