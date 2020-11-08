test("test EXP calculation", () => {
  /*global jest*/
  /*eslint no-undef: "error"*/
  const mock = jest.fn((likes) => 10 + 20*likes);

  let result = mock(5);

  expect(result).toBe(110);
  expect(mock).toHaveBeenCalled();
  expect(mock).toHaveBeenCalledTimes(1);
  expect(mock).toHaveBeenCalledWith(5);
});
