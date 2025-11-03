(function () {
	if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
		// Fallback: simply reveal all
		var all = document.querySelectorAll('[data-reveal]');
		for (var i = 0; i < all.length; i++) all[i].classList.add('is-visible');
		return;
	}

	var observer = new IntersectionObserver(function (entries) {
		for (var i = 0; i < entries.length; i++) {
			var e = entries[i];
			if (e.isIntersecting) {
				var el = e.target;
				var delay = el.getAttribute('data-reveal-delay');
				if (delay) {
					setTimeout(function (node) { node.classList.add('is-visible'); }.bind(null, el), parseInt(delay, 10));
				} else {
					el.classList.add('is-visible');
				}
				observer.unobserve(el);
			}
		}
	}, { rootMargin: '0px 0px -10% 0px', threshold: 0.1 });

	var targets = document.querySelectorAll('[data-reveal]');
	for (var j = 0; j < targets.length; j++) observer.observe(targets[j]);
})();





