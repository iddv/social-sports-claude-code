import random
import sys
import time

def ask_question(question: str) -> str:
    """Ask a question with styling and return the answer.
    
    If the question contains numbered options (like "1. Option one"), it will
    allow the user to respond with just the number.
    """
    # Skip displaying the system prompt
    if question == "System":
        return "System Prompt Set"
    
    # Check if the question has numbered options
    has_options = False
    options = []
    
    # Look for patterns like "1. Option" or "1) Option" in the question
    lines = question.split('\n')
    for line in lines:
        # Match patterns like "1. Option" or "1) Option" or "Option 1:"
        line = line.strip()
        if (line.startswith(('1.', '2.', '3.', '4.', '5.')) or 
            line.startswith(('1)', '2)', '3)', '4)', '5)')) or
            line.startswith(('Option 1:', 'Option 2:', 'Option 3:', 'Option 4:', 'Option 5:'))):
            has_options = True
            options.append(line)
        
    # Pick a random color for the question box
    colors = ['\033[91m', '\033[93m', '\033[92m', '\033[96m', '\033[94m', '\033[95m']
    color = random.choice(colors)
    reset = '\033[0m'
    
    # Draw a fancy box
    print(f"{color}╭{'─' * 78}╮{reset}")
    
    # Print the question with typing animation, preserving newlines
    paragraphs = question.split('\n')
    
    for paragraph in paragraphs:
        if not paragraph.strip():
            # Print empty line for paragraph breaks
            print()
            continue
            
        # Handle word wrapping within each paragraph
        words = paragraph.split()
        current_line = ""
        
        for word in words:
            if len(current_line) + len(word) + 1 <= 76:  # +1 for the space
                current_line += word + " "
            else:
                # Print the current line
                sys.stdout.write(f"{color}│{reset} {current_line}")
                sys.stdout.flush()
                time.sleep(0.03)  # Reduced pause time for better flow
                print()
                current_line = word + " "
        
        # Print the last line if there's anything left
        if current_line:
            sys.stdout.write(f"{color}│{reset} {current_line}")
            sys.stdout.flush()
            time.sleep(0.03)
            print()
    
    # Input section with cleaner styling
    print(f"{color}│{reset}")
    
    # If we have options, prompt specifically for a number
    if has_options:
        prompt = f"{color}│ > {reset}"
        answer = input(prompt)
        
        # If user entered just a number between 1-5, replace with the corresponding option text
        if answer.strip() in ['1', '2', '3', '4', '5']:
            option_num = int(answer.strip())
            
            # Find the corresponding option
            for opt in options:
                if opt.startswith(f"{option_num}.") or opt.startswith(f"{option_num})") or opt.startswith(f"Option {option_num}:"):
                    # Extract just the option text without the number
                    if opt.startswith(f"{option_num}."):
                        option_text = opt[len(f"{option_num}. "):].strip()
                    elif opt.startswith(f"{option_num})"):
                        option_text = opt[len(f"{option_num}) "):].strip()
                    else:  # Option 1: format
                        option_text = opt[len(f"Option {option_num}: "):].strip()
                    
                    # Print the selected option for clarity
                    print(f"{color}│ Selected: {option_text}{reset}")
                    answer = option_text
                    break
    else:
        # For multiline input, we use a different approach
        print(f"{color}│ > {reset}", end="")
        
        # Read input that can handle multiline pasted content
        answer_lines = []
        while True:
            try:
                # This allows for multiline input when content is pasted
                line = input()
                answer_lines.append(line)
                
                # Check if the user has finished input (empty line)
                if not line and answer_lines:
                    break
            except EOFError:
                # Handle EOF (Ctrl+D on Unix, Ctrl+Z on Windows)
                break
        
        # Join the lines for the complete answer
        answer = "\n".join(answer_lines)
    
    print(f"{color}╰{'─' * 78}╯{reset}")
    
    return answer 