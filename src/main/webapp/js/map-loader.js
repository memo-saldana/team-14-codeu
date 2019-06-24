let editMarker;
let map;
let displayMarkers = {};
 /**
 * Adds a new marker with a toggable info window
 * @param {Number} lat 
 * @param {Number} lng 
 * @param {String} description 
 */
function addDisplayMarker(lat, lng, description){
  const marker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map
  })
  const infoWindow = new google.maps.InfoWindow({
    content: buildDeletableMarker( lat, lng, description)
  })
  marker.addListener('click', () => {
    infoWindow.open(map, marker)
  })

  // Uses a dictionary with both lat and lng for faster lookup
  if(!displayMarkers[lat]){
    displayMarkers[lat] = {};
  } 
  displayMarkers[lat][lng] = marker;
}

/**
 * Main function
 */
function createMap(){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.764419, lng: -122.445911},
    zoom: 12
  });
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  })

  fetchMarkers();
}

 /** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
  fetch('/markers')
  .then((response) => response.json())
  .then((markers) => {
    markers.forEach((marker) => {
     addDisplayMarker(marker.lat, marker.lng, marker.content)
    });
  });
}

/** Creates a marker that shows a textbox the user can edit. */
function createMarkerForEdit(lat, lng){
  // If we're already showing an editable marker, then remove it.
  if(editMarker){
   editMarker.setMap(null);
  }
  editMarker = new google.maps.Marker({
    position: {lat: lat, lng: lng},
    map: map
  });
  const infoWindow = new google.maps.InfoWindow({
    content: buildInfoWindowInput(lat,lng)
  });
  // When the user closes the editable info window, remove the marker.
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });
  infoWindow.open(map, editMarker);
 }

/** Builds and returns HTML elements that show an editable textbox and a submit button. */
function buildInfoWindowInput(lat, lng){
  const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));
  button.onclick = () => {
    postMarker(lat, lng, textBox.value);
    addDisplayMarker(lat, lng, textBox.value);
    editMarker.setMap(null);
  };
  const containerDiv = document.createElement('div');
  containerDiv.appendChild(textBox);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(button);
  return containerDiv;
}

/** Sends a marker to the backend for saving. */
function postMarker(lat, lng, content){
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  params.append('content', content);
  fetch('/markers', {
    method: 'POST',
    body: params
  });
}

function buildDeletableMarker(lat, lng, content){ 
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Delete Marker'));
  button.onclick =() => {
    removeMarker(lat,lng, content);
  }
  const containerDiv = document.createElement('div');
  containerDiv.appendChild(document.createTextNode(content))
  containerDiv.appendChild(document.createElement('br'))
  containerDiv.appendChild(button)

  return containerDiv;
}

function removeMarker(lat, lng, content){
  const baseURL = window.location.protocol + '//' + window.location.host;  
  const url = new URL(baseURL+'/markers');
  url.searchParams.append('lat',lat);
  url.searchParams.append('lng',lng);
  url.searchParams.append('content',content);
  
  // Removes marker from datastore
  fetch(url, {
    method:'DELETE'
  })
  .then(response => response.json())
  .then( marker => {
    // Finds marker after being deleted from datastore,
    //  removes it from map, then from dictionary
    displayMarkers[lat][lng].setMap(null);
    delete displayMarkers[lat][lng];
  })
  .catch(error => console.log(error));
}