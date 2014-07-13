( function($) {
	jQuery(document).ready(function() {
	  jQuery.fn.center = function () {
	      this.css("position", "absolute");
	      this.css("top", ($(window).height() - this.height())/ 2 + $(window).scrollTop() + "px");
	      this.css("left", ($(window).width() - this.width()) / 2 + $(window).scrollLeft() + "px");
	      return this;
      };
      
      jQuery.fn.newHammerManager = function(element, options) {
    	  return new Hammer.Manager(element, options);
      };
      
	  jQuery.fn.newHammerTap = function(options) {
		  return new Hammer.Tap(options);
		};
		jQuery.fn.newHammerPress = function(options) {
			return new Hammer.Press(options);
		};
		jQuery.fn.newHammerPan = function(options) {
			return new Hammer.Pan(options);
		};
		jQuery.fn.newHammerPinch = function(options) {
			return new Hammer.Pinch(options);
		};
	});
} ) ( jQuery );