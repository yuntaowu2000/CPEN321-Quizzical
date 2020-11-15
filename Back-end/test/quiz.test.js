import * as lib from '~/Back-end/routes/quiz.js';

describe("Calculate Average function", () => {
  test("it should calculate the average score of quizScoreField values from the input data array", () => {
    const input = [
      { score1: 1, score2: 20 },
      { score1: 2, score2: 1  },
      { score1: 3, score2: 7  }
    ];

    const output1 = 2;
    const output2 = 14
    
    expect(lib.calculateAverage(input, "score1")).toEqual(output1);
    expect(lib.calculateAverage(input, "score2")).toEqual(output2);
    
  });
});

describe("Find Max Score function", () => {
  test("it should find the highest quizScoreField value in the input data array", () => {
    const input = [
      { score1: 1, score2: 20 },
      { score1: 2, score2: 1  },
      { score1: 3, score2: 7  }
    ];

    const output1 = 3;
    const output2 = 20
    
    expect(lib.findMaxScore(input, "score1")).toEqual(output1);
    expect(lib.findMaxScore(input, "score2")).toEqual(output2);
    
  });
});

