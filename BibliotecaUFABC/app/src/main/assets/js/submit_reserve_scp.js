var observer;
var expectedChange = null;

function getServerChange(){
    return expectedChange;
}

function getServerMessage(mutationsList, observer){
    let serverDiv = document.querySelector('#div-mensagemServidor');
    observer.disconnect();

    expectedChange = serverDiv == null ? '' : serverDiv.textContent;
}

function submitReservationForm(jsOptionsString){
    let jsOptions = JSON.parse(jsOptionsString);

    let library_button = document.querySelector('#biblioteca-button');
    let volume_button = document.querySelector('#volume-button');
    let year_button = document.querySelector('#ano-button');
    let edition_button = document.querySelector('#edicao-button');
    let support_button = document.querySelector('#suporte-button');

    if (library_button != null){
        let options = library_button.getElementsByTagName('option');
        for (let i = 0; i < options.length; ++i)
            if (jsOptions.library == i && !options[i].hasAttribute('selected'))
                options[i].setAttribute('selected', "");
            else if (jsOptions.library != i && options[i].hasAttribute('selected'))
                options[i].removeAttribute('selected');
    }

    if (volume_button != null){
        let options = volume_button.getElementsByTagName('option');
        for (let i = 0; i < options.length; ++i)
            if (jsOptions.volume == i && !options[i].hasAttribute('selected'))
                options[i].setAttribute('selected', "");
            else if (jsOptions.volume != i && options[i].hasAttribute('selected'))
                options[i].removeAttribute('selected');
    }

    if (year_button != null){
        let options = year_button.getElementsByTagName('option');
        for (let i = 0; i < options.length; ++i)
            if (jsOptions.year == i && !options[i].hasAttribute('selected'))
                options[i].setAttribute('selected', "");
            else if (jsOptions.year != i && options[i].hasAttribute('selected'))
                options[i].removeAttribute('selected');
    }

    if (edition_button != null){
        let options = edition_button.getElementsByTagName('option');
        for (let i = 0; i < options.length; ++i)
            if (jsOptions.edition == i && !options[i].hasAttribute('selected'))
                options[i].setAttribute('selected', "");
            else if (jsOptions.edition != i && options[i].hasAttribute('selected'))
                options[i].removeAttribute('selected');
    }

    if (support_button != null){
        let options = support_button.getElementsByTagName('option');
        for (let i = 0; i < options.length; ++i)
            if (jsOptions.support == i && !options[i].hasAttribute('selected'))
                options[i].setAttribute('selected', "");
            else if (jsOptions.support != i && options[i].hasAttribute('selected'))
                options[i].removeAttribute('selected');
    }

    let configs = { childList : true, subtree: true };
    let targetNode = document.querySelector('#div-mensagemServidor');

    observer = new MutationObserver(getServerMessage);
    observer.observe(targetNode, configs);

    document.querySelector('#botao_reservar').click();
}