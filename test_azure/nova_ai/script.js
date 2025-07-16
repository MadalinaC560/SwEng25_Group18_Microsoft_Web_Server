document.addEventListener("DOMContentLoaded", function () {
  const button = document.getElementById("cta-button");
  const message = document.getElementById("cta-message");

  button.addEventListener("click", function () {
    message.style.display = "inline-block";
    setTimeout(() => {
      message.style.display = "none";
    }, 3000);
  });

  // Fade-in scroll effect
  const faders = document.querySelectorAll('.fade-in');

  const appearOptions = {
    threshold: 0.2,
    rootMargin: "0px 0px -50px 0px"
  };

  const appearOnScroll = new IntersectionObserver(function (entries, observer) {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('visible');
      observer.unobserve(entry.target);
    });
  }, appearOptions);

  faders.forEach(fader => {
    appearOnScroll.observe(fader);
  });
});
