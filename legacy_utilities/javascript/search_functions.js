// Get labels for filters dynamically
function getOptionsLabel(){
  let labels = {
    fields : new Array(searchFields.length),
    filters : new Array(searchFilters.length),
    libraries : new Array(searchLibraries.length)
  };

  for (let i = 0; i < labels.fields.length; ++i)
    labels.fields[i] = searchFields[i].textContent;

  for (let i = 0; i < labels.filters.length; ++i)
    labels.filters[i] = searchFilters[i].textContent;

  for (let i = 0; i < labels.libraries.length; ++i)
    labels.libraries[i] = searchLibraries[i].textContent;

  return labels;
}

// Get filter state
function getSearchFiltersN(){
    let results = {
        field_get : 0,
        filters : new Array(searchFilters.length),
        libraries : new Array(searchLibraries.length)
    };

    for (let i = 0; i < searchFields.length; ++i)
        if (searchFields[i].hasAttribute("selected")){
            results.field_get = i;
            break;
        }

    for (let i = 0; i < searchFilters.length; ++i)
        results.filters[i] = JSON.parse(searchFilters[i].parentNode.getAttribute("aria-selected"));

    for (let i = 0; i < searchLibraries.length; ++i)
        results.libraries[i] = JSON.parse(searchLibraries[i].parentNode.getAttribute("aria-selected"));

    //js_api.log(JSON.stringify(results));
	return results;
}