import { Dependency } from "../../main/webroot/js/SubSystem";

jest.useFakeTimers();

describe("", () => {
    test("", async () => {
        let start = jest.fn((dep:Dependency<object>) => {
            console.log(dep.name);
            dep.forEach(d => {console.log(d.toString(), d.isStarted()); expect(d.isStarted() && false).toBeTruthy()});
            return Promise.resolve();
        });
        let milk = { name: "milk", depends: [], start: start, api: {} };
        let rennet = { name: "rennet", depends: ["milk"], start: start, api: {} };
        let cheese = { name: "cheese", depends: ["milk", "rennet"], start: start, api: {} };

        await Promise.all([
            Dependency.register(milk),
            Dependency.register(cheese),
            Dependency.register(rennet)
        ]);
        expect(start).toHaveBeenCalledTimes(3);
        // expect(start.mock.calls[2][0].name).toBe("cheese");
        console.log("DONE");

    });
});