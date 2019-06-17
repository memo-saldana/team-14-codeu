function createZipCodeMap(){
  fetch('/zipcode-data').then((response) => {
    return response.json();
  })
  .then((zipcodes) => {
      
    const map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: 35.78613674, lng: -119.4491591},
      zoom:5
    });

    var zipMarkers = zipcodes.map((zipcodeLocation) => {
      const marker =  new google.maps.Marker({
        position: {lat: zipcodeLocation.lat, lng: zipcodeLocation.lng},
        map: map
      });

      const info = new google.maps.InfoWindow({
        content: `${zipcodeLocation.zip} - ${zipcodeLocation.city}, ${zipcodeLocation.state}`
      });

      marker.addListener('click', () => {
        info.open(map, marker)
      });
      return marker;
    });
    
    var markerCluster = new MarkerClusterer(map, zipMarkers,
      {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
    
  });
}