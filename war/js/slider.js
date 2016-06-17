Slider = function(label, formHeight) {
	var self = this;

	var expSlider = document.createElement('div');
	expSlider.id = "expSlider";

	var expSliderLabel = document.createElement('div');
	expSliderLabel.id = "expSliderLabel";

	var labelFont = document.createElement('h4');
	labelFont.className = "expRotate";
	labelFont.innerHTML = label;

	var expSliderInnerContainer = document.createElement('div');
	expSliderInnerContainer.id = "expSliderInnerContainer";

	var expSliderSelect = document.createElement('div');
	expSliderSelect.id = "expSliderSelect";

	var expSliderForm = document.createElement('div');
	expSliderForm.id = "expSliderForm";

	var expSliderButton = document.createElement('div');
	expSliderButton.id = "expSliderButton";

	expSliderInnerContainer.style.height = formHeight + 'px';
	expSliderForm.style.maxHeight = (formHeight - 100) + 'px';
	expSlider.style.right = -315 + 'px';

	expSliderInnerContainer.appendChild(expSliderSelect);
	expSliderInnerContainer.appendChild(expSliderForm);
	expSliderInnerContainer.appendChild(expSliderButton);

	expSliderLabel.appendChild(labelFont);

	expSlider.appendChild(expSliderLabel);
	expSlider.appendChild(expSliderInnerContainer);

	document.getElementById('view').appendChild(expSlider);

	// Setup toggle mechanism
	var toggle = [ slideOut, slideIn ], c = 0;

	expSliderLabel.addEventListener('click', function(e) {
		e.preventDefault();
		toggle[c++ % 2]();
	});

	function slideOut() {
		var right = -315;

		function slideOutFrame() {
			right += 5;
			expSlider.style.right = right + 'px';
			if (right == 0)
				clearInterval(id);
		}
		var id = setInterval(slideOutFrame, 7);
	}

	function slideIn() {
		var right = 0;

		function slideInFrame() {
			right -= 5;
			expSlider.style.right = right + 'px';
			if (right == -315)
				clearInterval(id);
		}
		var id = setInterval(slideInFrame, 7);
	}

	// Setup welcome form
	var welcomeText = "Ich bin der Geist, der stets verneint!<br>"
			+ "Und das mit Recht; denn alles, was entsteht,<br>"
			+ "Ist wert, dass es zugrunde geht;<br>"
			+ "Drum besser waers, dass nichts entstuende.<br>"
			+ "So ist denn alles, was ihr Suende,<br>"
			+ "Zerstoerung, kurz, das Boese nennt,<br>"
			+ "Mein eigentliches Element.<br>";

	expSliderForm.innerHTML = welcomeText;

	// setup select and buttons
	var saveButton = document.createElement('button');
	saveButton.id = "expSaveBtn";
	saveButton.innerHTML = "Next &gt;&gt;";
	
	var backButton = document.createElement('button');
	backButton.id = "expBackBtn";
	backButton.innerHTML = "&lt;&lt; Back";

	expSliderButton.appendChild(saveButton);
	expSliderButton.appendChild(backButton);

}