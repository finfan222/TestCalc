package com.example.finfan.testcalc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // basic buttons list which contains all buttons in view
    private static final List<Button> BUTTON_LIST = new ArrayList<>();

    // button identify
    private TextView tvOutput, tvInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOutput = (TextView) findViewById(R.id.tvMain);
        tvInput = (TextView) findViewById(R.id.tvInput);

        for(Field field : R.id.class.getDeclaredFields()) {
        	if(!field.getName().startsWith("b")) {
        		continue;
			}
			field.setAccessible(true);
			try {
				Button button = (Button) findViewById(field.getInt(null));
				if(button == null) {
					continue;
				}
				BUTTON_LIST.add(button);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

        // set listeneres to buttons
        for(final Button next : BUTTON_LIST) {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isOperateButton(next) && next.getId() == R.id.bEqual) {
						double result = parse(toPostfixNotation(tvInput.getText().toString()));
						if(result != result) {
							return;
						}
						updateOutput(String.valueOf(result));
					} else if(next.getId() == R.id.bDel) {
                    	final String line = tvInput.getText().toString();
                    	StringBuilder sb = new StringBuilder(line.length());
                    	String[] values = line.split("\\s");
                    	for(int i = 0; i < values.length - 1; i++) {
                    		sb.append(values[i]).append(' ');
						}
						if(values.length > 1) {
							sb.deleteCharAt(sb.length() - 1);
						}
						tvInput.setText(sb.toString());
					} else if(next.getId() == R.id.bCe) {
                    	tvInput.setText("");
                    	tvOutput.setText("");
					}
					else {
                    	if(isOperateButton(next)) {
							updateInput(" " + next.getText() + " ");
						} else {
							updateInput(next.getText().toString());
						}
					}
                }
            });
        }
    }

    private boolean isOperateButton(Button btn) {
        final int btnId = btn.getId();
    	return btnId == R.id.bAdd
				|| btnId == R.id.bSub
				|| btnId == R.id.bDiv
				|| btnId == R.id.bMul
				|| btnId == R.id.bEqual
				|| btnId == R.id.bSqrt
				|| btnId == R.id.bMod;
    }

    /**
     * Update input text view and insert the text name of clicked button
     */
    private void updateInput(String input) {
    	tvInput.append(input);
    }

    /**
     * Show calculated results in cases:<br>
     *     <ul>
     *         <li>double click +</li>
     *         <li>double click -</li>
     *         <li>double click *</li>
     *         <li>double click /</li>
     *         <li>equal button click</li>
     *         <li>operation after already exist operation between two numbers</li>
     *     </ul>
     * After all - clear all values and opType
     * @param result the givable result values
     */
    private void updateOutput(String result) {
        tvOutput.setText(result);
        tvInput.setText("");
    }

	private static String toPostfixNotation(String evaluation) {
		StringBuilder sb = new StringBuilder(evaluation.length());
		String[] words = evaluation.split("\\s");

		String operation = null;
		for(int i = 0; i < words.length; i++) {
			final String word = words[i];
			if(isNumber(word)) {
				sb.append(word).append(' ');
				if(operation != null) {
					sb.append(operation).append(' ');
					operation = null;
				}
			} else {
				operation = word;
			}
		}

		return sb.toString();
	}

	private static final Pattern pattern = Pattern.compile("\\d+");
	private static boolean isNumber(String value) {
		return pattern.matcher(value).find();
	}

	/** @return result of evaluation or NAN if error */
	private static double parse(String evaluation) {
		Deque<Double> stack = new ArrayDeque<>();
		String[] words = evaluation.split("\\s");
		for(int i = 0; i < words.length; i++) {
			final String word = words[i];
			switch(word) {
				case "+":
					if(stack.size() < 2) {
						return Double.NaN;
					}
					stack.add(stack.poll() + stack.poll());
					break;
				case "×":
					if(stack.size() < 2) {
						return Double.NaN;
					}
					stack.add(stack.poll() * stack.poll());
					break;
				case "÷":
					if(stack.size() < 2) {
						return Double.NaN;
					}
					stack.add(stack.poll() / stack.poll());
					break;
				case "-":
					if(stack.size() < 2) {
						return Double.NaN;
					}
					stack.add(stack.poll() - stack.poll());
					break;
				case "√":
					if(stack.isEmpty()) {
						return Double.NaN;
					}
					stack.add(Math.sqrt(stack.poll()));
					break;
				case "%":
					if(stack.size()<2) {
						return Double.NaN;
					}
					stack.add(stack.poll() % stack.poll());
					break;
				default: //number
					stack.add(Double.parseDouble(word));
					break;
			}
		}

		if(stack.size() != 1) {
			return Double.NaN;
		}

		return stack.poll();
	}
}
