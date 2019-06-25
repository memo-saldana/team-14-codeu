function requestTranslation() {
  const text = document.getElementById('text').value;
  const languageCode = document.getElementById('language').value;

  const resultContainer = document.getElementById('result');
  resultContainer.innerText = 'Loading...';

  const params = new URLSearchParams();
  params.append('text', text);
  params.append('languageCode', languageCode);

  fetch('/translate', {
    method: 'POST',
    body: params
  }).then(response => response.text())
  .then((translatedMessage) => {
    resultContainer.innerText = translatedMessage;
  });
}
