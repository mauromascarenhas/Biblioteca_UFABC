// Selectable input references
var searchFields;
var searchFilters;
var searchLibraries;

// Input references
var searchButton;
var searchEditor;

var moreButton;

// Triggered as soon as DOM elements are available
function documentReady(){
    // Bind input references
    searchEditor = document.querySelector("#searc-basic");
    searchButton = document.querySelector("#bbotaobuscar").getElementsByTagName("input")[0];

    // Binds selectable input references
    searchFields = document.querySelector('select[name="bselcampo"]').getElementsByTagName("option");
    searchFilters = document.querySelector('#bselmaterial-menu').getElementsByTagName('a');
    searchLibraries = document.querySelector("#bselbiblioteca-menu").getElementsByTagName("a");
}

function setSearchFilter(filterStr){
    let filter = JSON.parse(filterStr);
    if (filter.field == null || filter.filters == null || filter.libraries == null) return;

    for (let i = 0; i < searchFields.length; ++i)
        if (filter.field == i && !searchFields[i].hasAttribute("selected"))
            searchFields[i].setAttribute("selected", "");
        else if (filter.field != i && searchFields[i].hasAttribute("selected"))
            searchFields[i].removeAttribute("selected");

    for (let i = 0; i < searchFilters.length; ++i)
        if (filter.filters[i].toString() != searchFilters[i].parentNode.getAttribute("aria-selected"))
            searchFilters[i].click();

    for (let i = 0; i < searchLibraries.length; ++i)
        if (filter.libraries[i].toString() != searchLibraries[i].parentNode.getAttribute("aria-selected"))
            searchLibraries[i].click();
}

// Performs a search with the given query
function performSearch(query){
    setSearchFilter(js_api.getSearchFilters());
    searchEditor.value = query;
    searchButton.click();
}

function loadMoreBooks(){
    document.querySelector('#botaoCarregarMais').click();
}

// Callback for new results
function addSearchResults(nodeList, observer){
    var newNodes = nodeList[0].addedNodes;
    var results = new Array(newNodes.length - 2);

    for (let i = 2; i < newNodes.length; ++i){
        let nodeElement = newNodes[i];
        let nodeProperties = newNodes[i].getElementsByTagName('p');

        let bookElement = {
            title : nodeProperties[0].getElementsByClassName('tituloResultadoBusca')[0].textContent,
            author : nodeProperties[1].textContent,
            type : nodeProperties[2].textContent,
            section : nodeProperties[3].textContent,
            code : new URL(nodeElement.getElementsByTagName('a')[0].href).searchParams.get('codigo')
        };

        results[i - 2] = bookElement;
    }

    if (moreButton.onclick == null) observer.disconnect();
    js_api.setSearchResults(JSON.stringify(results), moreButton.onclick != null);
}

// Get search results
function getSearchResults(){
    moreButton = document.querySelector('#botaoCarregarMais');

    var container = document.getElementById('lista');
    if (container == null){
        js_api.setSearchResults(JSON.stringify(new Array(0), null, 4), false);
        return;
    }

    var list = container.getElementsByTagName('li');
    var results = new Array(list.length - 2);

    for (let i = 1; i < list.length - 1; ++i){
        let nodeElement = list[i];
        let nodeProperties = list[i].getElementsByTagName('p');

        let bookElement = {
            title : nodeProperties[0].getElementsByClassName('tituloResultadoBusca')[0].textContent,
            author : nodeProperties[1].textContent,
            type : nodeProperties[2].textContent,
            section : nodeProperties[3].textContent,
            code : new URL(nodeElement.getElementsByTagName('a')[0].href).searchParams.get('codigo')
        };

        results[i - 1] = bookElement;
    }

    if (moreButton == null){
        js_api.setSearchResults(JSON.stringify(results), false);
        return;
    }

    if (moreButton.onclick != null){
        let config = { childList : true };
        let observer = new MutationObserver(addSearchResults);
        observer.observe(container, config);
    }

    js_api.setSearchResults(JSON.stringify(results), moreButton.onclick != null);
}