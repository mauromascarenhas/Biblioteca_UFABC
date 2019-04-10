function performLogin(username, password){
    document.getElementById('username').value = username;
    document.getElementById('password').value = password;
    document.getElementsByName('_eventId_proceed')[0].click();
}

function checkForErrors(){
    var error = document.querySelector('.form-error');
    var hasError = error != null;
    js_api.showError(hasError, hasError ? error.textContent : "");
}

function getUsername(){
    var logoutButton = document.querySelector("#li-logout");
    js_api.setUserName(logoutButton == null? "" : logoutButton.getElementsByTagName('span')[1].textContent.trim());
}