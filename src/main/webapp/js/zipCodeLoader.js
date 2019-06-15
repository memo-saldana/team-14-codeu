function createZipCodeMap(){
  fetch('/zipcode-data').then((response) => {
    return response.json();
  }).then((zipcodes) => {
      
    const map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: 35.78613674, lng: -119.4491591},
      zoom:5
    });

    zipcodes.forEach((zipcodeLocation) => {
      new google.maps.Marker({
        position: {lat: zipcodeLocation.lat, lng: zipcodeLocation.lng},
        map: map
      });  
    });
  });
}