function getNewestBooks(){
    var list = document.getElementById('lista').getElementsByTagName('li');
    var results = new Array(list.length - 2);

    for (let i = 1; i < list.length - 1; ++i){
      let nodeElement = list[i];
      let nodeProperties = list[i].getElementsByTagName('p');

      let bookElement = {
        title : nodeProperties[0].getElementsByClassName('tituloResultadoBusca')[0].textContent,
        author : nodeProperties[1].textContent,
        type : nodeProperties[2].textContent,
        section : nodeProperties[3].textContent,
        code : new URL(list[i].getElementsByTagName('a')[0].href).searchParams.get('codigo')
      };

      results[i - 1] = bookElement;
    }

    return results;
}

function checkLoginStatus(){
    var logoutButton = document.querySelector("#li-logout");
    var connected = logoutButton != null;
    return {
        status : connected,
        name : connected ? logoutButton.getElementsByTagName('span')[1].textContent.trim() : ""
    };
}