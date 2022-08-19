// https://stackoverflow.com/questions/29085197/how-do-you-json-stringify-an-es6-map
export const mapToObj = (m: Map<string, unknown>) => {
    return Array.from(m).reduce((obj, [key, value]) => {
        if (!key.startsWith('__')) {
            obj[key] = value;
        }
        return obj;
    }, {});
};
export type Optionals<T> = {
    [K in OptionalKeys<RemoveIndex<T>>]-?: Required<T>[K];
};
/* https://stackoverflow.com/a/49579497 */
export type OptionalKeys<T> = {
    [K in keyof T]-?: {} extends { [P in K]: T[K] } ? K : never;
}[keyof T];
/* https://stackoverflow.com/a/66252656 */
export type RemoveIndex<T> = {
    [P in keyof T as string extends P
        ? never
        : number extends P
        ? never
        : P]: T[P];
};
