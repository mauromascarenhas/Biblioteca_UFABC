function getReservationFormData(){
    let library_button = document.querySelector('#biblioteca-button');
    let volume_button = document.querySelector('#volume-button');
    let year_button = document.querySelector('#ano-button');
    let edition_button = document.querySelector('#edicao-button');
    let support_button = document.querySelector('#suporte-button');

    if (library_button != null){
        let options = library_button.getElementsByTagName('option');
        var library_options = new Array(options.length);

        for (let i = 0; i < options.length; ++i)
            library_options[i] = options[i].textContent;
    }
    else library_options = new Array(0);

    if (volume_button != null){
        let options = volume_button.getElementsByTagName('option');
        var volume_options = new Array(options.length);

        for (let i = 0; i < options.length; ++i)
            volume_options[i] = options[i].textContent;
    }
    else volume_options = new Array(0);

    if (year_button != null){
        let options = year_button.getElementsByTagName('option');
        var year_options = new Array(options.length);

        for (let i = 0; i < options.length; ++i)
            year_options[i] = options[i].textContent;
    }
    else year_options = new Array(0);

    if (edition_button != null){
        let options = edition_button.getElementsByTagName('option');
        var edition_options = new Array(options.length);

        for (let i = 0; i < options.length; ++i)
            edition_options[i] = options[i].textContent;
    }
    else edition_options = new Array(0);

    if (support_button != null){
        let options = support_button.getElementsByTagName('option');
        var support_options = new Array(options.length);

        for (let i = 0; i < options.length; ++i)
            support_options[i] = options[i].textContent;
    }
    else support_options = new Array(0);

    let options = {
        library : library_options,
        volume : volume_options,
        year : year_options,
        edition : edition_options,
        support : support_options
    };

    js_api_r.setOptions(JSON.stringify(options));
}

function detectAction(){
    let form = document.querySelector('#frm_dados_comp');
    if (form != null) getReservationFormData();
    else js_api_r.getServerMessage(document.querySelector('#div-conteudoCentral').textContent.trim());
}