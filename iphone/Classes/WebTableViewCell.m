//
//  WebTableViewCell.m
//  Titanium
//
//  Created by Blain Hamon on 6/20/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "WebTableViewCell.h"

@implementation WebTableViewCell
@synthesize htmlLabel;

- (id)initWithFrame:(CGRect)frame reuseIdentifier:(NSString *)reuseIdentifier;
{
	self = [super initWithFrame:frame reuseIdentifier:reuseIdentifier];
	if (self != nil){
		UIView * cellContentView = [self contentView];
		htmlLabel = [[UIWebView alloc] initWithFrame:[cellContentView frame]];
		[htmlLabel setAutoresizingMask:UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight];
		[htmlLabel setAlpha:0.0];
		[htmlLabel setDelegate:self];
		[htmlLabel setExclusiveTouch:NO];
		[htmlLabel setUserInteractionEnabled:NO];
		[htmlLabel setBackgroundColor:[UIColor clearColor]];
		[htmlLabel setOpaque:NO];
		[cellContentView addSubview:htmlLabel];
	}
	return self;
}

- (void)prepareForReuse;
{
	[htmlLabel setAlpha:0.0];
	[super prepareForReuse];
}

- (void)updateState: (BOOL) animated;
{
		if ([self isHighlighted]) {
			[htmlLabel stringByEvaluatingJavaScriptFromString:@"document.body.style['color']='white';"];
		} else if ([self accessoryType] == UITableViewCellAccessoryCheckmark){
			[htmlLabel stringByEvaluatingJavaScriptFromString:@"document.body.style['color']='#374F82';"];
		} else {
			[htmlLabel stringByEvaluatingJavaScriptFromString:@"document.body.style['color']='black';"];
		}

}

- (void)setHighlighted:(BOOL)hilighted animated:(BOOL)animated;
{
	[super setHighlighted:hilighted animated:animated];
	[self updateState:animated];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated;
{
	[super setSelected:selected animated:animated];
	[self updateState:animated];
}


- (void)dealloc {
	[htmlLabel setDelegate:nil];
	[htmlLabel release];
    [super dealloc];
}

- (void)webViewDidFinishLoad:(UIWebView *)inputWebView;
{
	[self updateState:NO];
	[UIView beginAnimations:@"webView" context:nil];
	[inputWebView setAlpha:1.0];
	[UIView commitAnimations];
}	

@end