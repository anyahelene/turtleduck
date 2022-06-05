

// https://stackoverflow.com/questions/29085197/how-do-you-json-stringify-an-es6-map
const mapToObj = m => {
    return Array.from(m).reduce((obj, [key, value]) => {
        if (!key.startsWith('__')) {
            obj[key] = value;
        }
        return obj;
    }, {});
};
